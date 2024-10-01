package bo.imorochi.microservice.istore.user.service.query;

import bo.imorochi.microservice.istore.core.model.PaymentDetails;
import bo.imorochi.microservice.istore.core.model.User;
import bo.imorochi.microservice.istore.core.query.FetchUserPaymentDetailsQuery;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class UserEventsHandler {

    @QueryHandler
    public User findUserPaymentDetails(FetchUserPaymentDetailsQuery query) {
        
        PaymentDetails paymentDetails = PaymentDetails.builder()
                .cardNumber("123Card")
                .cvv("123")
                .name("ISAIAS MOROCHI")
                .validUntilMonth(12)
                .validUntilYear(2030)
                .build();

        User user = User.builder()
                .firstName("Isaias")
                .lastName("Morochi")
                .userId(query.getUserId())
                .paymentDetails(paymentDetails)
                .build();

        return user;
    }
    
    
}
