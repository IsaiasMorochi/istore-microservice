package bo.imorochi.microservice.istore.product.service.query;

import bo.imorochi.microservice.istore.core.events.ProductReservedEvent;
import bo.imorochi.microservice.istore.product.service.core.data.ProductEntity;
import bo.imorochi.microservice.istore.product.service.core.data.ProductRepository;
import bo.imorochi.microservice.istore.product.service.core.events.ProductCreatedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("product-group")
public class ProductEventsHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductEventsHandler.class);

    private final ProductRepository productsRepository;

    @Autowired
    public ProductEventsHandler(ProductRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    @ExceptionHandler(resultType = Exception.class)
    public void handle(Exception exception) throws Exception {
        throw exception;
    }

    /**
     * Este metodo solo controlara la excpcion de esta clase y no es general como @ControllerAdvice
     *
     * @param exception
     */
    @ExceptionHandler(resultType = IllegalArgumentException.class)
    public void handle(IllegalArgumentException exception) {
        // Log error message
    }

    @EventHandler
    public void on(ProductCreatedEvent event) {

        ProductEntity productEntity = new ProductEntity();
        BeanUtils.copyProperties(event, productEntity);

        try {
            this.productsRepository.save(productEntity);
        } catch (IllegalArgumentException ex) {
            LOGGER.error(ex.getMessage());
        }

        // simulacion error con ProductsServiceEventsErrorHandler
        // if (true) throw new Exception("Forcing exception in the Event Handler class");
    }

    @EventHandler
    public void on(ProductReservedEvent productReservedEvent) {

        ProductEntity productEntity = this.productsRepository.findByProductId(productReservedEvent.getProductId());

        LOGGER.debug("ProductReservedEvent: Current product quantity {}", productEntity.getQuantity());

        productEntity.setQuantity(productEntity.getQuantity() - productReservedEvent.getQuantity());

        this.productsRepository.save(productEntity);

        LOGGER.debug("ProductReservedEvent: New product quantity {}", productEntity.getQuantity());

        LOGGER.info("ProductReservedEvent is called for productId: {} and orderId: {}",
                productReservedEvent.getProductId(), productReservedEvent.getOrderId());
    }

}
