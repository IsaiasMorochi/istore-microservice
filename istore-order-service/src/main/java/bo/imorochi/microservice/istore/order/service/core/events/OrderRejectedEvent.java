package bo.imorochi.microservice.istore.order.service.core.events;

import bo.imorochi.microservice.istore.order.service.core.model.OrderStatus;
import lombok.Value;

@Value
public class OrderRejectedEvent {
	private final String orderId;
	private final String reason;
	private final OrderStatus orderStatus = OrderStatus.REJECTED;
}
