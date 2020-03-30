package cn.jeremy.wechat.controller;

import cn.jeremy.wechat.service.ApiAuthorityService;
import cn.jeremy.wechat.service.DemonStockService;
import cn.jeremy.wechat.service.StockDataHandlerService;
import cn.jeremy.wechat.stock.ThsMockTrade;
import cn.jeremy.wechat.entity.DemonStock;
import com.alibaba.fastjson.JSONObject;
import java.util.List;
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

    @Autowired
    StockDataHandlerService stockDataHandlerService;

    @Autowired
    ApiAuthorityService apiAuthorityService;

    @Autowired
    DemonStockService demonStockService;

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

    @RequestMapping(value = "/genfile", method = RequestMethod.GET)
    public String genfile()
    {
        thsMockTrade.genStockDayReport();
        return "success";
    }

    @RequestMapping(value = "/importData", method = RequestMethod.GET)
    public String importData()
    {
        stockDataHandlerService.importData();
        return "success";
    }

    @RequestMapping(value = "/queryNearestDemonStock", method = RequestMethod.GET)
    public List<DemonStock> queryNearestDemonStock()
    {
        return demonStockService.queryNearestDemonStock();
    }

    @RequestMapping(value = "/genDemonStockPic", method = RequestMethod.GET)
    public String genDemonStockPic()
    {
        List<DemonStock> demonStockData = demonStockService.queryNearestDemonStock();
        return stockDataHandlerService.genDemonStockPic(demonStockData);
    }

    @RequestMapping(value = "/addLeftNum/{userId}", method = RequestMethod.GET)
    public String addLeftNum(@PathVariable Integer userId)
    {
        int count = apiAuthorityService.addLeftNum(userId, 1);
        return count == 1 ? "success" : "failed";
    }

    @RequestMapping(value = "/uploadDemonStockHistoryMedia", method = RequestMethod.GET)
    public String uploadDemonStockHistoryMedia()
    {
        stockDataHandlerService.uploadDemonStockHistoryMedia();
        return "success";
    }

    @RequestMapping(value = "/queryDemonStockHistory", method = RequestMethod.GET)
    public String queryDemonStockHistory()
    {
        return JSONObject.toJSONString(stockDataHandlerService.queryDemonStockHistory(0));
    }

}
