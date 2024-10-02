package bo.imorochi.microservice.istore.order.service.saga;

import bo.imorochi.microservice.istore.core.commands.CancelProductReservationCommand;
import bo.imorochi.microservice.istore.core.commands.ProcessPaymentCommand;
import bo.imorochi.microservice.istore.core.commands.ReserveProductCommand;
import bo.imorochi.microservice.istore.core.events.PaymentProcessedEvent;
import bo.imorochi.microservice.istore.core.events.ProductReservationCancelledEvent;
import bo.imorochi.microservice.istore.core.events.ProductReservedEvent;
import bo.imorochi.microservice.istore.core.model.User;
import bo.imorochi.microservice.istore.core.query.FetchUserPaymentDetailsQuery;
import bo.imorochi.microservice.istore.order.service.command.commands.ApproveOrderCommand;
import bo.imorochi.microservice.istore.order.service.command.commands.RejectOrderCommand;
import bo.imorochi.microservice.istore.order.service.core.events.OrderApprovedEvent;
import bo.imorochi.microservice.istore.order.service.core.events.OrderCreatedEvent;
import bo.imorochi.microservice.istore.order.service.core.events.OrderRejectedEvent;
import bo.imorochi.microservice.istore.order.service.core.model.OrderSummary;
import bo.imorochi.microservice.istore.order.service.query.FindOrderQuery;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Permitira manejar los eventos y emitir los comandos
 * Marcamos las propiedades con transient para que no se serialice
 */
@Saga
public class OrderSaga {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderSaga.class);

    @Autowired
    private transient QueryGateway queryGateway;

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient DeadlineManager deadlineManager;

    @Autowired
    private transient QueryUpdateEmitter queryUpdateEmitter;

    private String scheduleId;

    private final String PAYMENT_PROCESSING_TIMEOUT_DEADLINE = "payment-processing-deadline";


    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent orderCreatedEvent) {

        ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .orderId(orderCreatedEvent.getOrderId())
                .productId(orderCreatedEvent.getProductId())
                .quantity(orderCreatedEvent.getQuantity())
                .userId(orderCreatedEvent.getUserId())
                .build();

        LOGGER.info("OrderCreatedEvent handled for orderId: {} and productId: {}"
                , reserveProductCommand.getOrderId(), reserveProductCommand.getProductId());

        this.commandGateway.send(reserveProductCommand, (commandMessage, commandResultMessage) -> {

            if (commandResultMessage.isExceptional()) {
                // Start a compensating transaction
                LOGGER.warn("RejectOrderCommand handled for orderId: {} and productId: {}"
                        , reserveProductCommand.getOrderId(), reserveProductCommand.getProductId());
                RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(orderCreatedEvent.getOrderId(),
                        commandResultMessage.exceptionResult().getMessage());

                this.commandGateway.send(rejectOrderCommand);
            }

        });

    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent productReservedEvent) {

        // Process user payment
        LOGGER.info("ProductReservedEvent is called for productId: {} and orderId: {}",
                productReservedEvent.getProductId(), productReservedEvent.getOrderId());

        FetchUserPaymentDetailsQuery fetchUserPaymentDetailsQuery =
                new FetchUserPaymentDetailsQuery(productReservedEvent.getUserId());

        User userPaymentDetails = null;

        try {
            userPaymentDetails = this.queryGateway.query(fetchUserPaymentDetailsQuery,
                    ResponseTypes.instanceOf(User.class)).join();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());

            // Start compensating transaction
            LOGGER.warn("FetchUserPaymentDetailsQuery Start compensating transaction for userId: {}",
                    productReservedEvent.getUserId());
            cancelProductReservation(productReservedEvent, ex.getMessage());
            return;
        }

        if (userPaymentDetails == null) {
            // Start compensating transaction
            cancelProductReservation(productReservedEvent, "Could not fetch user payment details");
            return;
        }

        LOGGER.info("Successfully fetched user payment details for user {}",
                userPaymentDetails.getFirstName());

        // si el procesamiento de pago no se completa en 120s esperamos que se active un evento diferente
        scheduleId = deadlineManager.schedule(Duration.of(120, ChronoUnit.SECONDS),
                PAYMENT_PROCESSING_TIMEOUT_DEADLINE, productReservedEvent);

        // TODO: agregado intencionalmente para probar el deadline, evita que se procese el pago
        //if (true) return;

        ProcessPaymentCommand proccessPaymentCommand = ProcessPaymentCommand.builder()
                .orderId(productReservedEvent.getOrderId())
                .paymentDetails(userPaymentDetails.getPaymentDetails())
                .paymentId(UUID.randomUUID().toString())
                .build();

        String result = null;
        try {
            result = commandGateway.sendAndWait(proccessPaymentCommand);
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            // Start compensating transaction
            LOGGER.warn("ProcessPaymentCommand Start compensating transaction for orderId: {}",
                    productReservedEvent.getOrderId());
            cancelProductReservation(productReservedEvent, ex.getMessage());
            return;
        }

        if (result == null) {
            // Start compensating transaction
            LOGGER.warn("The ProcessPaymentCommand resulted in NULL. Initiating a compensating transaction");
            cancelProductReservation(productReservedEvent, "Could not proccess user payment with provided payment details");
        }

    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent) {

        cancelDeadline();

        // Send an ApproveOrderCommand
        ApproveOrderCommand approveOrderCommand =
                new ApproveOrderCommand(paymentProcessedEvent.getOrderId());

        this.commandGateway.send(approveOrderCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent) {
        LOGGER.info("Order is approved. Order Saga is complete for orderId: {}",
                orderApprovedEvent.getOrderId());

        //una vez este metodo se ejecuta ya no se podra gestionar nuevos eventos para el saga
        //SagaLifecycle.end();

        this.queryUpdateEmitter.emit(
                FindOrderQuery.class,
                query -> true,
                new OrderSummary(orderApprovedEvent.getOrderId(),
                        orderApprovedEvent.getOrderStatus(),
                        "")
        );

    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservationCancelledEvent productReservationCancelledEvent) {
        // Create and send a RejectOrderCommand
        RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(productReservationCancelledEvent.getOrderId(),
                productReservationCancelledEvent.getReason());

        this.commandGateway.send(rejectOrderCommand);
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderRejectedEvent orderRejectedEvent) {
        LOGGER.info("Successfully rejected order with id {}", orderRejectedEvent.getOrderId());

        //Encargado de emitir a las consultas con suscripcion sobre actualizaciones
        this.queryUpdateEmitter.emit(
                FindOrderQuery.class,
                query -> true,
                new OrderSummary(orderRejectedEvent.getOrderId(),
                        orderRejectedEvent.getOrderStatus(),
                        orderRejectedEvent.getReason()
                )
        );

    }


    // Method Aux
    private void cancelProductReservation(ProductReservedEvent productReservedEvent, String reason) {

        CancelProductReservationCommand publishProductReservationCommand =
                CancelProductReservationCommand.builder()
                        .orderId(productReservedEvent.getOrderId())
                        .productId(productReservedEvent.getProductId())
                        .quantity(productReservedEvent.getQuantity())
                        .userId(productReservedEvent.getUserId())
                        .reason(reason)
                        .build();

        this.commandGateway.send(publishProductReservationCommand);

    }

    /**
     * Cancela el deadline si el pago es procesado correctamente y no se vence el plazo programado
     */
    private void cancelDeadline() {
        if (scheduleId != null) {
            deadlineManager.cancelSchedule(PAYMENT_PROCESSING_TIMEOUT_DEADLINE, scheduleId);
            scheduleId = null;
        }
    }

    /**
     * Se ejecuta cuando se supera el umblar de espera
     *
     * @param productReservedEvent Evento que emite el deadline
     */
    @DeadlineHandler(deadlineName = PAYMENT_PROCESSING_TIMEOUT_DEADLINE)
    public void handlePaymentDeadline(ProductReservedEvent productReservedEvent) {
        LOGGER.info("Payment processing deadline took place. Sending a compensating command to cancel the product reservation");
        cancelProductReservation(productReservedEvent, "Payment timeout");
    }

}
