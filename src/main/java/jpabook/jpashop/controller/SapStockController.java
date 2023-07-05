package jpabook.jpashop.controller;

import jpabook.jpashop.dto.ResultResDataDto;
import jpabook.jpashop.service.SapStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
@Controller
@RequiredArgsConstructor
public class SapStockController {
    private final SapStockService sapStockService;
    @PostMapping("/sapStock/distribute")
    @ResponseBody
    public ResultResDataDto distribute(@RequestParam(value = "count") Long qty, @RequestParam(value = "item") Long itemId) {
        ResultResDataDto resultResDataDto = sapStockService.distribute(itemId,qty);
        return resultResDataDto;
    }
}
