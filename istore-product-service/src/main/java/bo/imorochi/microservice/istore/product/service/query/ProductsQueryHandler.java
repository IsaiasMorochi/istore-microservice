package bo.imorochi.microservice.istore.product.service.query;

import bo.imorochi.microservice.istore.product.service.core.data.ProductEntity;
import bo.imorochi.microservice.istore.product.service.core.data.ProductRepository;
import bo.imorochi.microservice.istore.product.service.query.rest.ProductRestModel;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProductsQueryHandler {
	
	private final ProductRepository productRepository;
	
	public ProductsQueryHandler(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}
	
	@QueryHandler
	public List<ProductRestModel> findProducts(FindProductsQuery query) {
		
		List<ProductRestModel> productsRest = new ArrayList<>();
		
		List<ProductEntity> storedProducts =  this.productRepository.findAll();
		
		for(ProductEntity productEntity: storedProducts) {
			ProductRestModel productRestModel = new ProductRestModel();
			BeanUtils.copyProperties(productEntity, productRestModel);
			productsRest.add(productRestModel);
		}
		
		return productsRest;
		
	}

	@QueryHandler
	public ProductRestModel findProductById(FindProductQuery query) {

		ProductEntity productEntity =  this.productRepository.findByProductId(query.getProductId());

		ProductRestModel productRestModel = new ProductRestModel();
		BeanUtils.copyProperties(productEntity, productRestModel);

		return productRestModel;

	}

}
