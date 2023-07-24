package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.DeliveryStatus;
import jpabook.jpashop.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery,Long> {

    Optional<Delivery> findByOrderAndStatus(Order order, DeliveryStatus ready);
}
