package jpabook.jpashop.repository;

import jpabook.jpashop.domain.OrderItem;
import jpabook.jpashop.dto.OrderItemDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {

    @Query(value = "select new jpabook.jpashop.dto.OrderItemDto(oi.item,sum(oi.orderPrice) , sum(oi.count),oi.status) from OrderItem oi " +
            "left join Order o  " +
            "where o.id=:id " +
            "group by oi.item,oi.status")
    List<OrderItemDto> findOrderItemDtos(@Param("id") Long id);
}
