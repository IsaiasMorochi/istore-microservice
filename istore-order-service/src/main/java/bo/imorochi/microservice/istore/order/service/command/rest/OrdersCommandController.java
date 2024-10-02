package bo.imorochi.microservice.istore.order.service.command.rest;

import bo.imorochi.microservice.istore.order.service.command.commands.CreateOrderCommand;
import bo.imorochi.microservice.istore.order.service.core.model.OrderStatus;
import bo.imorochi.microservice.istore.order.service.core.model.OrderSummary;
import bo.imorochi.microservice.istore.order.service.query.FindOrderQuery;
import jakarta.validation.Valid;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrdersCommandController {

    private final QueryGateway queryGateway;

    private final CommandGateway commandGateway;

    @Autowired
    public OrdersCommandController(QueryGateway queryGateway,
                                   CommandGateway commandGateway) {
        this.queryGateway = queryGateway;
        this.commandGateway = commandGateway;
    }

    @PostMapping
    public OrderSummary createOrder(@Valid @RequestBody OrderCreateRest order) {

        String userId = "27b95829-4f3f-4ddf-8983-151ba010e35b";
        String orderId = UUID.randomUUID().toString();

        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .orderId(orderId)
                .userId(userId)
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .addressId(order.getAddressId())
                .orderStatus(OrderStatus.CREATED)
                .build();

        // Subscription Query
        try (SubscriptionQueryResult<OrderSummary, OrderSummary> queryResult = this.queryGateway.subscriptionQuery(
                new FindOrderQuery(orderId), ResponseTypes.instanceOf(OrderSummary.class), ResponseTypes.instanceOf(OrderSummary.class))) {

            commandGateway.sendAndWait(createOrderCommand);
            return queryResult.updates().blockFirst();

        }

    }

}
