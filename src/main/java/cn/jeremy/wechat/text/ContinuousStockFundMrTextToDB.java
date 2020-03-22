package cn.jeremy.wechat.text;

import cn.jeremy.wechat.entity.ContinuousStockFund;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class ContinuousStockFundMrTextToDB extends BaseMrTextToDB<ContinuousStockFund> {

    private static final String UPDATE_BY_NUM_SQL = "update continuous_stock_fund set one_day_trade = ?, " +
            "three_day_trade = ?, five_day_trade = ?, ten_day_trade = ?, twenty_day_trade = ?, other_day_trade = ? " +
            "where num = ?";

    private static final String INSERT_SQL = "insert into continuous_stock_fund (num, name, one_day_trade, " +
            "three_day_trade, " +
            "five_day_trade, ten_day_trade, twenty_day_trade, other_day_trade) values (?, ?, ?, ?, ?, ?, ?, ?)";

    public ContinuousStockFundMrTextToDB(String dateStr, JdbcTemplate jdbcTemplate, String path) {
        super(dateStr, jdbcTemplate, path);
    }

    @Override
    String getFileTag() {
        return "fund";
    }


    @Override
    public int insertDB(List<ContinuousStockFund> result) {
        if (!CollectionUtils.isEmpty(result)) {
           result.forEach(s -> {
               int update = jdbcTemplate.update(UPDATE_BY_NUM_SQL, new Object[]{s.getOneDayTrade(),
                       s.getThreeDayTrade(),
                       s.getFiveDayTrade(), s.getTenDayTrade(), s.getTwentyDayTrade(), s.getOtherDayTrade(),
                       s.getNum()});
               if (update == 0) {
                   jdbcTemplate.update(INSERT_SQL,new Object[]{s.getNum(),s.getName(),s.getOneDayTrade(),
                           s.getThreeDayTrade(),
                           s.getFiveDayTrade(), s.getTenDayTrade(), s.getTwentyDayTrade(), s.getOtherDayTrade()});
               }
           });
           return result.size();
        }
        return 0;
    }

    @Override
    public ContinuousStockFund lineToObject(String line) {
        if (StringUtils.isEmpty(line)) {
            return null;
        }
        String[] split = line.split("\t");
        if (null != split && split.length > 2) {
            ContinuousStockFund data = new ContinuousStockFund(split[1], split[0]);
            for (int i = 2; i < split.length; i++) {
                data.fillValue(split[i]);
            }
            return data;
        }
        return null;
    }
}
