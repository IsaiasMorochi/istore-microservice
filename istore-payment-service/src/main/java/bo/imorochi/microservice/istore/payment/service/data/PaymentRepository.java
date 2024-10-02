package bo.imorochi.microservice.istore.payment.service.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository <PaymentEntity, String>{

}
