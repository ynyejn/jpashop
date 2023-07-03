package jpabook.jpashop.repository;

import jpabook.jpashop.domain.stock.ChannelStock;
import jpabook.jpashop.domain.stock.ChannelStockList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StockListRepository extends JpaRepository<ChannelStockList,Long> {
    @Query("select csl from ChannelStockList csl " +
            " join csl.channelStock cs " +
            " join csl.account a" +
            " join csl.item i " +
            " where a.accountCode=:acode " +
            " and i.productCode=:pcode " +
            " and csl.qty>=:count")
    Optional<ChannelStockList> checkStock(@Param("acode") String accountCode, @Param("pcode") String productCode, @Param("count") int count);

}
