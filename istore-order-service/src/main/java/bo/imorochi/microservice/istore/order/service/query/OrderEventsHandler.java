package bo.imorochi.microservice.istore.order.service.query;

import bo.imorochi.microservice.istore.order.service.core.data.OrderEntity;
import bo.imorochi.microservice.istore.order.service.core.data.OrdersRepository;
import bo.imorochi.microservice.istore.order.service.core.events.OrderApprovedEvent;
import bo.imorochi.microservice.istore.order.service.core.events.OrderCreatedEvent;
import bo.imorochi.microservice.istore.order.service.core.events.OrderRejectedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("order-group")
public class OrderEventsHandler {

    private final OrdersRepository ordersRepository;

    @Autowired
    public OrderEventsHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @EventHandler
    public void on(OrderCreatedEvent event) throws Exception {
        OrderEntity orderEntity = new OrderEntity();
        BeanUtils.copyProperties(event, orderEntity);

        this.ordersRepository.save(orderEntity);
    }


    @EventHandler
    public void on(OrderApprovedEvent orderApprovedEvent) {
        OrderEntity orderEntity = this.ordersRepository.findByOrderId(orderApprovedEvent.getOrderId());

        if (orderEntity == null) {
            throw new IllegalStateException(
                    String.format("Order with orderId %s not exist", orderApprovedEvent.getOrderId())
            );
        }

        orderEntity.setOrderStatus(orderApprovedEvent.getOrderStatus());

        this.ordersRepository.save(orderEntity);

    }

    @EventHandler
    public void on(OrderRejectedEvent orderRejectedEvent) {
        OrderEntity orderEntity = this.ordersRepository.findByOrderId(orderRejectedEvent.getOrderId());
        orderEntity.setOrderStatus(orderRejectedEvent.getOrderStatus());
        this.ordersRepository.save(orderEntity);
    }

}
