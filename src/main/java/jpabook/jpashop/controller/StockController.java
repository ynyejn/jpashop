package jpabook.jpashop.controller;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.domain.stock.EcomStock;
import jpabook.jpashop.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;
    @GetMapping("/stocks")
    public String stockList(Model model){
        List<EcomStock> stocks = stockService.findStocks();
        model.addAttribute("stocks",stocks);
        return "stocks/stockList";
    }
}
