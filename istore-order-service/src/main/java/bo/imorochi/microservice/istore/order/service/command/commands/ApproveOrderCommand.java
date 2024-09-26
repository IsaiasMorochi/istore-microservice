package bo.imorochi.microservice.istore.order.service.command.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
public class ApproveOrderCommand {

	@TargetAggregateIdentifier
	private final String orderId;
}
