package bo.imorochi.microservice.istore.order.service.core.model;

import lombok.Value;

@Value
public class OrderSummary {

	private final String orderId;
	private final OrderStatus orderStatus;
	private final String message;
	
}
