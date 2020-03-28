package cn.jeremy.wechat.service;

import cn.jeremy.wechat.entity.StockCloseData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;

/**
 * 处理stock_{num}表相关的数据
 *
 * @author fengjiangtao
 * @date 2020/3/25 22:10
 */
@Service
public class StockCloseService
{

    private final JdbcTemplate jdbcTemplate;

    public StockCloseService(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final static String SELECT_BY_DATE = "select * from stock_%s where today > ? order by today limit 6";

    public List<StockCloseData> selectByDate(Date date, String num, String name)
    {
        String sql = String.format(SELECT_BY_DATE, num);
        List<StockCloseData> stockCloseDataList = new ArrayList<>();
        jdbcTemplate.query(sql, new Object[] {date}, new StockCloseRowCallBackHandler(stockCloseDataList));
        stockCloseDataList.forEach(s -> {
            s.setName(name);
            s.setNum(num);
        });
        if (stockCloseDataList.size() > 0)
        {
            return stockCloseDataList;
        }

        return null;
    }

    public static class StockCloseRowCallBackHandler implements RowCallbackHandler
    {
        private List<StockCloseData> stockCloseDataList;

        public StockCloseRowCallBackHandler(List<StockCloseData> stockCloseDataList)
        {
            this.stockCloseDataList = stockCloseDataList;
        }

        @Override
        public void processRow(ResultSet resultSet)
            throws SQLException
        {
            StockCloseData stockCloseData = new StockCloseData(resultSet.getDate(11));
            stockCloseData.setId(resultSet.getInt(1));
            stockCloseData.setOpenPrice(resultSet.getInt(2));
            stockCloseData.setTopPrice(resultSet.getInt(3));
            stockCloseData.setLowPrice(resultSet.getInt(4));
            stockCloseData.setYestClosePrice(resultSet.getInt(5));
            stockCloseData.setClosePrice(resultSet.getInt(6));
            stockCloseData.setChg(resultSet.getInt(7));
            stockCloseData.setZlc(resultSet.getInt(8));
            stockCloseData.setZlr(resultSet.getInt(9));
            stockCloseData.setJe(resultSet.getInt(10));
            stockCloseDataList.add(stockCloseData);
        }
    }
}
