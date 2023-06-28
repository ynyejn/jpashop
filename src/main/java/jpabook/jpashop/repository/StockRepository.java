package jpabook.jpashop.repository;

import jpabook.jpashop.domain.Account;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.stock.ChannelStock;
import jpabook.jpashop.dto.ProductStockDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<ChannelStock,Long> {
    @Query("select cs from ChannelStock cs " +
            " join cs.account a" +
            " join cs.item i " +
            " where a.accountCode=:acode " +
            " and i.productCode=:pcode " +
            " and cs.status='가용재고' " +
            " and cs.stock>=:count")
    Optional<ChannelStock> checkStock(@Param("acode") String accountCode, @Param("pcode") String productCode, @Param("count") int count);

    @Query(value = "SELECT SUM(STOCK) as stock ,PRODUCT_CODE as productCode " +
            "FROM CHANNEL_STOCK where STATUS='가용재고' " +
            "group by PRODUCT_CODE"
            ,nativeQuery = true)
    List<ProductStockInterface> findAllStockGroupByProduct();

    ChannelStock findByItemAndAccountAndStatus(Item product, Account account, String status);

    interface ProductStockInterface{
        int getStock();
        String getProductCode();
    }
}
