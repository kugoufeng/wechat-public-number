package cn.jeremy.wechat.service;

import cn.jeremy.wechat.entity.BaseStockData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class StockBaseService
{

    @Autowired
    JdbcTemplate jdbcTemplate;


    private static final String SELECT_BY_NUM = "select name from stock_base where num = ?";

    public String selectNameByNum(String num)
    {
        BaseStockData stockData = new BaseStockData();
        jdbcTemplate.query(SELECT_BY_NUM,new Object[]{num},resultSet -> {
            stockData.setName(resultSet.getString(1));
        });
        return stockData.getName();
    }

}
