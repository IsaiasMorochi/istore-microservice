package bo.imorochi.microservice.istore.product.service.core.errorhandling;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class ErrorMessage {

	private final String message;
	private final Date timestamp;
	
}
