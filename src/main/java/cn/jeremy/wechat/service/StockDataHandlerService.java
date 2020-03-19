package cn.jeremy.wechat.service;

import cn.jeremy.common.utils.DateTools;
import cn.jeremy.wechat.text.DemonStockMrTextToDB;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * 将文本文件内容导入数据库中
 */
@Service
public class StockDataHandlerService {

    @Value("${file.download.basepath}")
    private String basePath;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final String FILE_NAME = "part-r-00000";

    private static final String COUNT_FROM_DEMON_STOCK_SQL = "select count(1) from demon_stock where num = ? and Date" +
            "(select_date) = ?";

    private static final String INSERT_TO_DEMON_STOCK_SQL = "insert into demon_stock (num, name, select_date) values " +
            "(?, ?, ?)";

    @Scheduled(cron = "0 31 17 ? * MON-FRI")
    public void importData() {
        String date = DateTools.date2TimeStr(new Date(), DateTools.DATE_FORMAT_10);
        new DemonStockMrTextToDB(date, basePath, jdbcTemplate).execInsertDB();
    }

}
