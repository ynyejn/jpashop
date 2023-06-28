package jpabook.jpashop.controller;

import jpabook.jpashop.domain.stock.ChannelStock;
import jpabook.jpashop.dto.ResultResDataDto;
import jpabook.jpashop.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;
    @GetMapping("/stocks")
    public String stockList(Model model){
        List<ChannelStock> stocks = stockService.findStocks();
        int sapStock = stockService.getSapStock();
        model.addAttribute("sapStock",sapStock);
        model.addAttribute("stocks",stocks);
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
