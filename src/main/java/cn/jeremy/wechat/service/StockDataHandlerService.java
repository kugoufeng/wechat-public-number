package cn.jeremy.wechat.service;

import cn.jeremy.common.utils.DateTools;
import cn.jeremy.wechat.stock.bean.SelectStockData;
import cn.jeremy.wechat.text.ContinuousStockFundMrTextToDB;
import cn.jeremy.wechat.text.DemonStockMrTextToDB;

import java.util.ArrayList;
import java.util.Date;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 将文本文件内容导入数据库中
 */
@Service
public class StockDataHandlerService
{

    @Value("${file.download.basepath}")
    private String basePath;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final String FILE_NAME = "part-r-00000";

    private static final String SELECT_MAX_DATE_DEMON_STOCK_SQL =
        "SELECT * from `demon_stock` where select_date = (SELECT max(select_date) FROM `demon_stock`)";

    @Scheduled(cron = "0 35 18 ? * MON-FRI")
    public void importData()
    {
        String date = DateTools.date2TimeStr(new Date(), DateTools.DATE_FORMAT_10);
        new DemonStockMrTextToDB(date, jdbcTemplate, basePath).execInsertDB();
        new ContinuousStockFundMrTextToDB(date, jdbcTemplate, basePath).execInsertDB();
    }

    /**
     * 查询最近日期推荐的股票
     *
     * @return java.util.List<cn.jeremy.wechat.stock.bean.SelectStockData>
     * @throws
     * @author fengjiangtao
     */
    public List<SelectStockData> queryNearestDemonStock()
    {
        List<SelectStockData> result = new ArrayList<>();
        jdbcTemplate.query(SELECT_MAX_DATE_DEMON_STOCK_SQL, new Object[] {}, resultSet -> {
            String num = resultSet.getString(2);
            String name = resultSet.getString(3);
            Date date = new Date(resultSet.getDate(4).getTime());
            result.add(new SelectStockData(name, num, date));
        });
        return result;
    }

}
