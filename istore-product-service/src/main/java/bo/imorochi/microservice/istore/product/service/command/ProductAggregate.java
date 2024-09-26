package bo.imorochi.microservice.istore.product.service.command;

import bo.imorochi.microservice.istore.product.service.core.events.ProductCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;

@Aggregate
public class ProductAggregate {
	
	@AggregateIdentifier
	private String productId;
	private String title;
	private BigDecimal price;
	private Integer quantity;
	
	public ProductAggregate() {
		
	}
	
	@CommandHandler
	public ProductAggregate(CreateProductCommand createProductCommand) throws Exception {
		// Validate Create Product Command

		if(createProductCommand.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Price cannot be less or equal than zero");
		}

		if(createProductCommand.getTitle() == null
				|| createProductCommand.getTitle().isBlank()) {
			throw new IllegalArgumentException("Title cannot be empty");
		}

		ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent();

		// similar a un mapper, las propiedades deben tener el mismo nombre o seran ignoradas
		BeanUtils.copyProperties(createProductCommand, productCreatedEvent);

		// Se ejecute .apply se llamara al metodo anotado con @EventSourcingHandler
		AggregateLifecycle.apply(productCreatedEvent);

		//simulacion de error en @CommandHandler Exception
       //if (true) throw new Exception("An error took place in the CreateProductComand @CommandHandler");
	}

	/**
	 * Evitar agregar logica de negocio, solo deberia actualizar el estado del agregado
	 * @param productCreatedEvent Recibe la actualizacion del agregado inmendaimente se ejecuta el metodo apply
	 */
	@EventSourcingHandler
	public void on(ProductCreatedEvent productCreatedEvent) {
		this.productId = productCreatedEvent.getProductId();
		this.title = productCreatedEvent.getTitle();
		this.price = productCreatedEvent.getPrice();
		this.quantity = productCreatedEvent.getQuantity();
	}

}
