package bo.imorochi.microservice.istore.product.service.command.rest;

import bo.imorochi.microservice.istore.product.service.command.CreateProductCommand;
import jakarta.validation.Valid;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping(value = "/products")
public class ProductRestController {

    private final Environment env;
    private final CommandGateway commandGateway;


    @Autowired
    public ProductRestController(Environment env,
                                 CommandGateway commandGateway) {
        this.env = env;
        this.commandGateway = commandGateway;
    }

//    @GetMapping
//    public String getProduct() {
//        return "HTTP GET Handled " + env.getProperty("local.server.port");
//    }

    @PostMapping
    public String createProduct(@Valid @RequestBody CreateProductRestModel createProductRestModel) {

        CreateProductCommand createProductCommand = CreateProductCommand.builder()
                .productId(UUID.randomUUID().toString())
                .title(createProductRestModel.getTitle())
                .price(createProductRestModel.getPrice())
                .quantity(createProductRestModel.getQuantity())
                .build();

        String returnValue;

        returnValue = commandGateway.sendAndWait(createProductCommand);

        //Command Gateway asimilar como un API para enviar comandos al Command Bus
//		try {
//			returnValue = commandGateway.sendAndWait(createProductCommand);
//		} catch (Exception ex) {
//			returnValue = ex.getLocalizedMessage();
//		}

        return returnValue;

    }

//    @PutMapping
//    public String updateProduct() {
//        return "HTTP PUT Handled";
//    }
//
//    @DeleteMapping
//    public String deleteProduct() {
//        return "HTTP DELETE handled";
//    }

}
