package cn.jeremy.wechat.service;

import cn.jeremy.wechat.entity.BaseStockData;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class StockBaseService
{

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final String SELECT_BY_NUM = "select name from stock_base where num = ?";

    private static final String SELECT_BY_NAME = "select num from stock_base where name like ?";

    public String selectNameByNum(String num)
    {
        BaseStockData stockData = new BaseStockData();
        jdbcTemplate.query(SELECT_BY_NUM, new Object[] {num}, resultSet -> {
            stockData.setName(resultSet.getString(1));
        });
        return stockData.getName();
    }

    public String selectNumByName(String name)
    {
        List<String> numList = new ArrayList<>();
        jdbcTemplate.query(SELECT_BY_NAME, new Object[] {"%" + name + "%"}, resultSet -> {
            numList.add(resultSet.getString(1));
        });
        if (numList.size() == 1)
        {
            return numList.get(0);
        }
        return null;
    }

}
