package bo.imorochi.microservice.istore.product.service.command;

import bo.imorochi.microservice.istore.product.service.core.data.ProductLookupEntity;
import bo.imorochi.microservice.istore.product.service.core.data.ProductLookupRepository;
import bo.imorochi.microservice.istore.product.service.core.events.ProductCreatedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("product-group")
public class ProductLookupEventsHandler {

	private final ProductLookupRepository productLookupRepository;
	
	public ProductLookupEventsHandler(ProductLookupRepository productLookupRepository) {
		this.productLookupRepository = productLookupRepository;
	}

	@EventHandler
	public void on(ProductCreatedEvent event) {
		
		ProductLookupEntity productLookupEntity = new ProductLookupEntity(event.getProductId(),
				event.getTitle());
		
		this.productLookupRepository.save(productLookupEntity);
		
	}

	@ResetHandler
	public void reset() {
		this.productLookupRepository.deleteAll();
	}
	
}
