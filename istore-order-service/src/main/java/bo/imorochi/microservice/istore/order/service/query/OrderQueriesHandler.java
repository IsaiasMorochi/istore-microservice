package bo.imorochi.microservice.istore.order.service.query;

import bo.imorochi.microservice.istore.order.service.core.data.OrderEntity;
import bo.imorochi.microservice.istore.order.service.core.data.OrdersRepository;
import bo.imorochi.microservice.istore.order.service.core.model.OrderSummary;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderQueriesHandler {

    private final OrdersRepository ordersRepository;

    @Autowired
    public OrderQueriesHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @QueryHandler
    public OrderSummary findOrder(FindOrderQuery findOrderQuery) {

        OrderEntity orderEntity = this.ordersRepository.findByOrderId(findOrderQuery.getOrderId());
        return new OrderSummary(orderEntity.getOrderId(), orderEntity.getOrderStatus(), "");

    }

}
