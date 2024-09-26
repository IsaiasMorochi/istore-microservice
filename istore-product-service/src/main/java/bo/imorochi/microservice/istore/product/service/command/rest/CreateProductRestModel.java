package bo.imorochi.microservice.istore.product.service.command.rest;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateProductRestModel {

	@NotNull
	@NotBlank(message="Product title is a required field")
	@Pattern(regexp = "^[A-Za-z0-9\\s]+$")
	private String title;
	
	@Min(value=1, message="Price cannot be lower than 1")
	private BigDecimal price;
	
	@Min(value=1, message="Quantity cannot be lower than 1")
	@Max(value=5, message="Quantity cannot be larger than 5")
	private Integer quantity;

}
