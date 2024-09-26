package bo.imorochi.microservice.istore.product.service.query.rest;

import bo.imorochi.microservice.istore.product.service.query.FindProductQuery;
import bo.imorochi.microservice.istore.product.service.query.FindProductsQuery;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductQueryController {
	
	private final QueryGateway queryGateway;

	public ProductQueryController(QueryGateway queryGateway) {
		this.queryGateway = queryGateway;
	}

	@GetMapping
	public List<ProductRestModel> getProducts() {
		
		FindProductsQuery findProductsQuery = new FindProductsQuery();

		return this.queryGateway.query(findProductsQuery,
				ResponseTypes.multipleInstancesOf(ProductRestModel.class)).join();

	}

	@GetMapping(value = "/{productId}")
	public ProductRestModel getByProductId(@PathVariable("productId") String productId) {

		FindProductQuery findProductQuery = new FindProductQuery();
		findProductQuery.setProductId(productId);

		return this.queryGateway.query(findProductQuery,
				ResponseTypes.instanceOf(ProductRestModel.class)).join();

	}

}
