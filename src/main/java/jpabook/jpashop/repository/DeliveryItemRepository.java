package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Delivery;
import jpabook.jpashop.domain.DeliveryItem;
import jpabook.jpashop.domain.DeliveryStatus;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.item.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeliveryItemRepository extends JpaRepository<DeliveryItem,Long> {

    DeliveryItem findByDeliveryAndItem(Delivery originalDelivery, Item item);
}
