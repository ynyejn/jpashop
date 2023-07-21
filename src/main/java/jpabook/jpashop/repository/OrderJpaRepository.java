package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.stock.ChannelStockList;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderJpaRepository extends JpaRepository<Order,Long> {
    @Query(value = "select o from Order o left join OrderItem oi " +
            "where o.status=:status and oi.item=:product")
    List<Order> findAllByItemAndStatus(@Param("product") Item product,@Param("status") OrderStatus status);

    @Override
    @EntityGraph(attributePaths = {"account","member"})
    List<Order> findAll();
}
