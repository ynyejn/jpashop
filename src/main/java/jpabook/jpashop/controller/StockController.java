package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.stock.ChannelStock;
import jpabook.jpashop.domain.stock.ChannelStockList;
import jpabook.jpashop.dto.ChannelStockListDto;
import jpabook.jpashop.dto.ResultResDataDto;
import jpabook.jpashop.service.ItemService;
import jpabook.jpashop.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;
    private final ItemService itemService;
    @GetMapping("/stocks")
    public String stockList(Model model){
        List<Item> items = itemService.findItems();
        List<ChannelStockListDto> stocks = stockService.findStocks();
        int sapStock = stockService.getSapStock();
        model.addAttribute("sapStock",sapStock);
        model.addAttribute("stocks",stocks);
        model.addAttribute("items",items);
        return "stocks/stockList";
    }

    @PostMapping("/stock/distribute")
    @ResponseBody
    public ResultResDataDto distributeStock() {
        ResultResDataDto resultResDataDto = stockService.distributeStock();
        return resultResDataDto;
    }
    @PostMapping("/stock/redistribute")
    @ResponseBody
    public ResultResDataDto redistributeStock() {
        ResultResDataDto resultResDataDto = stockService.redistributeStock();
        return resultResDataDto;
    }

}
