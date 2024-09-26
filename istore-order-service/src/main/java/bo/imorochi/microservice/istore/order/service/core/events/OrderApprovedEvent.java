package bo.imorochi.microservice.istore.order.service.core.events;

import bo.imorochi.microservice.istore.order.service.core.model.OrderStatus;
import lombok.Value;

@Value
public class OrderApprovedEvent {

	private final String orderId;
	private final OrderStatus orderStatus = OrderStatus.APPROVED;
	
}
