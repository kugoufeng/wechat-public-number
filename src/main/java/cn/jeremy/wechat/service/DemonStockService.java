package cn.jeremy.wechat.service;

import cn.jeremy.common.utils.DateTools;
import cn.jeremy.wechat.entity.DemonStock;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author fengjiangtao
 * @date 2020/3/28 17:00
 */
@Service
@Slf4j
public class DemonStockService
{
    private final JdbcTemplate jdbcTemplate;

    public DemonStockService(JdbcTemplate jdbcTemplate)
    {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final String SELECT_MAX_DATE_DEMON_STOCK_SQL =
        "SELECT * from `demon_stock` where select_date = (SELECT max(select_date) FROM `demon_stock`)";

    private static final String SELECT_DATA_OF_MONTH =
        "select * from demon_stock where select_date >= ? and select_date <= ? order by select_date";

    /**
     * 查询最近日期推荐的妖股
     *
     * @return java.util.List<cn.jeremy.wechat.entity.SelectStockData>
     * @throws
     * @author fengjiangtao
     */
    public List<DemonStock> queryNearestDemonStock()
    {
        List<DemonStock> result = new ArrayList<>();
        jdbcTemplate.query(SELECT_MAX_DATE_DEMON_STOCK_SQL, new DemonStockRowCallBackHandel(result));
        return emptyListToNull(result);
    }

    /**
     * 查询其它月的推荐数据
     *
     * @param offset 月份偏移量
     * @return java.util.List<cn.jeremy.wechat.entity.DemonStock>
     * @throws
     * @author fengjiangtao
     */
    public List<DemonStock> queryOtherMonthData(int offset)
    {
        if (offset >= 0)
        {
            return null;
        }
        Date currentMonthDate =
            DateTools.timeStr2Date(DateTools.getCurrentDate(DateTools.DATE_FORMAT_7), DateTools.DATE_FORMAT_7);
        String lastMonth =
            DateTools.addDate(currentMonthDate.getTime(), offset, DateTools.MONTH, DateTools.DATE_FORMAT_7);
        return queryDemonStocksByMonth(lastMonth);
    }

    /**
     * 查询当月的推荐数据
     *
     * @return java.util.List<cn.jeremy.wechat.entity.DemonStock>
     * @throws
     * @author fengjiangtao
     */
    public List<DemonStock> queryCurrentMonthData()
    {
        String currentMonth = DateTools.getCurrentDate(DateTools.DATE_FORMAT_7);
        return queryDemonStocksByMonth(currentMonth);
    }

    /**
     * 查询某个月推荐的全部妖股
     *
     * @param month yyyy-MM
     * @return java.util.List<cn.jeremy.wechat.entity.DemonStock>
     * @throws
     * @author fengjiangtao
     */
    public List<DemonStock> queryDemonStocksByMonth(String month)
    {
        Date date = DateTools.timeStr2Date(month, DateTools.DATE_FORMAT_7);
        Date beginTimeOfMonth = DateTools.getBeginTimeOfMonth(date);
        Date endTimeOfMonth = DateTools.getEndTimeOfMonth(date);
        List<DemonStock> list = new ArrayList<>();
        jdbcTemplate.query(SELECT_DATA_OF_MONTH,
            new Object[] {beginTimeOfMonth, endTimeOfMonth},
            new DemonStockRowCallBackHandel(list));

        return emptyListToNull(list);
    }

    private List<DemonStock> emptyListToNull(List<DemonStock> list)
    {
        if (CollectionUtils.isEmpty(list))
        {
            return null;
        }
        return list;
    }

    public static class DemonStockRowCallBackHandel implements RowCallbackHandler
    {
        private List<DemonStock> list;

        public DemonStockRowCallBackHandel(List<DemonStock> list)
        {
            this.list = list;
        }

        @Override
        public void processRow(ResultSet resultSet)
            throws SQLException
        {
            DemonStock demonStock = new DemonStock();
            demonStock.setId(resultSet.getInt(1));
            demonStock.setNum(resultSet.getString(2));
            demonStock.setName(resultSet.getString(3));
            demonStock.setDay(resultSet.getInt(4));
            demonStock.setChg(resultSet.getInt(5));
            demonStock.setJe(resultSet.getInt(6));
            demonStock.setSelectChg(resultSet.getInt(7));
            demonStock.setSelectJe(resultSet.getInt(8));
            demonStock.setSelectDate(new Date(resultSet.getDate(9).getTime()));
            list.add(demonStock);
        }
    }

}
