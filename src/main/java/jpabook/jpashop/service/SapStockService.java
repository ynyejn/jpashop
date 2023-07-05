package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.stock.SapStock;
import jpabook.jpashop.dto.ResultResDataDto;
import jpabook.jpashop.repository.SapStockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SapStockService {
    private final SapStockRepository sapStockRepository;
    private final ItemService itemService;

    public ResultResDataDto distribute(Long itemId, Long qty) {
        try {
            SapStock sapStock = new SapStock();
            Item item = itemService.findItem(itemId);
            sapStock.setItem(item);
            sapStock.setStock(qty);
            sapStock.setDistributionFlag(0);
            sapStockRepository.save(sapStock);
            return ResultResDataDto.fromResMsg(true, "성공");
        }catch (Exception e){
            return ResultResDataDto.fromResMsg(false, "실패");

        }
    }
}
