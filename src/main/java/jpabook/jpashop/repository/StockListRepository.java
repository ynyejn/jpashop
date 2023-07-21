package jpabook.jpashop.repository;

import jakarta.persistence.LockModeType;
import jpabook.jpashop.domain.Account;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.stock.ChannelStock;
import jpabook.jpashop.domain.stock.ChannelStockList;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StockListRepository extends JpaRepository<ChannelStockList,Long> {

    @Override
    @EntityGraph(attributePaths = {"channelStock","account"})
    List<ChannelStockList> findAll();
    @Query("select csl from ChannelStockList csl " +
            " join csl.channelStock cs " +
            " join csl.account a" +
            " join csl.item i " +
            " where a.accountCode=:acode " +
            " and i.productCode=:pcode " +
            " and csl.qty>=:count order by csl.id desc limit 1 ")
    Optional<ChannelStockList> checkStock(@Param("acode") String accountCode, @Param("pcode") String productCode, @Param("count") Long count);

    @Query(value = "select sum(QTY) as stock ,a.PRODUCT_CODE as productCode from CHANNEL_STOCK_LIST as a " +
            "left join CHANNEL_STOCK as b on(a.CS_IDX=b.CS_IDX) " +
            "left join (select MAX(CS_IDX) as CS_IDX ,PRODUCT_CODE " +
            "               from CHANNEL_STOCK group by PRODUCT_CODE) as c on(a.CS_IDX=c.CS_IDX) " +
            " group by a.PRODUCT_CODE,a.CS_IDX"
        ,nativeQuery = true)
    List<ProductStockInterface> findAllStockGroupByProduct();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    ChannelStockList findPessimisticById(Long id);

    Optional<ChannelStockList> findByAccountAndItem(Account account, Item item);

    interface ProductStockInterface{
        int getStock();
        String getProductCode();
    }
}
