package jpabook.jpashop.repository;

import jpabook.jpashop.domain.stock.SapStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SapStockRepository extends JpaRepository<SapStock,Long> {
    @Query("select nullif(sum(ss.stock),0) from SapStock ss where ss.distributionFlag=0")
    Integer findSapStock();

    List<SapStock> findAllByDistributionFlag(int flag);
}
