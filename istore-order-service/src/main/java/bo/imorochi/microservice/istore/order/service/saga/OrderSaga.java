package bo.imorochi.microservice.istore.order.service.saga;

import bo.imorochi.microservice.istore.core.commands.ReserveProductCommand;
import bo.imorochi.microservice.istore.core.events.ProductReservedEvent;
import bo.imorochi.microservice.istore.order.service.command.commands.RejectOrderCommand;
import bo.imorochi.microservice.istore.order.service.core.events.OrderApprovedEvent;
import bo.imorochi.microservice.istore.order.service.core.events.OrderCreatedEvent;
import bo.imorochi.microservice.istore.order.service.core.model.OrderSummary;
import bo.imorochi.microservice.istore.order.service.query.FindOrderQuery;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
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

            }

        });

//        commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {
//
//            @Override
//            public void onResult(CommandMessage<? extends ReserveProductCommand> commandMessage,
//                                 CommandResultMessage<? extends Object> commandResultMessage) {
//                if(commandResultMessage.isExceptional()) {
//                    // Start a compensating transaction
////                    RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(orderCreatedEvent.getOrderId(),
////                            commandResultMessage.exceptionResult().getMessage());
////
////                    commandGateway.send(rejectOrderCommand);
//                }
//
//            }
//
//        });

    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent productReservedEvent) {

        // Process user payment
        LOGGER.info("ProductReservedEvent is called for productId: {} and orderId: {}",
                productReservedEvent.getProductId(), productReservedEvent.getOrderId());

    }

//    @EndSaga
//    @SagaEventHandler(associationProperty="orderId")
//    public void handle(OrderApprovedEvent orderApprovedEvent) {
//        LOGGER.info("Order is approved. Order Saga is complete for orderId: " + orderApprovedEvent.getOrderId());
//        //SagaLifecycle.end();
//        queryUpdateEmitter.emit(FindOrderQuery.class, query -> true,
//                new OrderSummary(orderApprovedEvent.getOrderId(),
//                        orderApprovedEvent.getOrderStatus(),
//                        ""));
//    }




}
