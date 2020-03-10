package cn.jeremy.wechat.controller;

import cn.jeremy.wechat.stock.ThsMockTrade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stock")
public class StockController
{
    @Autowired
    ThsMockTrade thsMockTrade;

    @RequestMapping(value = "/updateAMarkStocks", method = RequestMethod.GET)
    public String updateAMarkStocks()
    {
        //异步操作
        thsMockTrade.updateAMarkStocks();
        return "success";
    }

    @RequestMapping(value = "/updateStockCloseData", method = RequestMethod.GET)
    public String updateStockCloseData()
    {
        //异步操作
        thsMockTrade.updateStockCloseData();
        return "success";
    }

    @RequestMapping(value = "/updateStockCloseData/{num}", method = RequestMethod.GET)
    public String updateStockCloseData(@PathVariable String num)
    {
        thsMockTrade.updateStockCloseData(num);
        return "success";
    }
}