package cn.jeremy.wechat.text;

import cn.jeremy.common.utils.DateTools;
import cn.jeremy.wechat.stock.bean.SelectStockData;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

/**
 * @author fengjiangtao
 * @date 2020/3/19 23:17
 */
public class DemonStockMrTextToDB extends BaseMrTextToDB<SelectStockData>
{
    private String dateStr;

    private Date date;

    private static final String FILE_NAME = "part-r-00000";

    private static final String COUNT_FROM_DEMON_STOCK_SQL = "select count(1) from demon_stock where num = ? and Date" +
        "(select_date) = ?";

    private static final String INSERT_TO_DEMON_STOCK_SQL = "insert into demon_stock (num, name, select_date) values " +
        "(?, ?, ?)";

    public DemonStockMrTextToDB(String dateStr, String path, JdbcTemplate jdbcTemplate)
    {
        this.dateStr = dateStr;
        this.jdbcTemplate = jdbcTemplate;
        this.path = path;
        this.date = DateTools.timeStr2Date(dateStr, DateTools.DATE_FORMAT_10);
    }

    @Override
    String getFileTag()
    {
        return "demon";
    }

    @Override
    FilenameFilter createFilenameFilter()
    {
        return new FilenameFilter()
        {
            @Override
            public boolean accept(File file, String name)
            {
                File parentFile = null;
                if ((parentFile = file.getParentFile()) != null)
                {
                    return dateStr.equals(parentFile.getName()) && getFileTag().equals(file.getName()) &&
                        name.equals(FILE_NAME);
                }
                return false;
            }
        };
    }

    @Override
    public void insertDB(List<SelectStockData> result)
    {
        if (!CollectionUtils.isEmpty(result))
        {
            result.forEach(s -> {
                Integer count = queryForObject(COUNT_FROM_DEMON_STOCK_SQL,
                    new Object[] {s.getNum(), s.getSelectDate()},
                    Integer.class);
                if (count == null || count == 0)
                {
                    jdbcTemplate.update(INSERT_TO_DEMON_STOCK_SQL,
                        new Object[] {s.getNum(), s.getName(),
                            s.getSelectDate()});
                }
            });
        }
    }

    @Override
    public SelectStockData lineToObject(String line)
    {
        if (StringUtils.isEmpty(line))
        {
            return null;
        }
        String[] split = line.split("\t");
        if (null != split && split.length > 2)
        {
            SelectStockData selectStockData = new SelectStockData(date);
            selectStockData.setNum(split[0]);
            selectStockData.setName(split[1]);
            return selectStockData;
        }
        return null;
    }
}
