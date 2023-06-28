package jpabook.jpashop.repository;

import jpabook.jpashop.domain.stock.DistributionRate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistributionRateRepository extends JpaRepository<DistributionRate,Long> {
}
