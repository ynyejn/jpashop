package jpabook.jpashop.repository;

import jpabook.jpashop.domain.stock.EcomStock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<EcomStock,Long> {
}
