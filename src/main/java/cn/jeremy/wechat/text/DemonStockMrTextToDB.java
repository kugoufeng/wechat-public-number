package cn.jeremy.wechat.text;

import cn.jeremy.wechat.entity.DemonStock;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author fengjiangtao
 * @date 2020/3/19 23:17
 */
public class DemonStockMrTextToDB extends BaseMrTextToDB<DemonStock>
{

    private static final String COUNT_FROM_DEMON_STOCK_SQL = "select count(1) from demon_stock where num = ? and Date" +
        "(select_date) = ?";

    private static final String INSERT_TO_DEMON_STOCK_SQL = "insert into demon_stock (num, name, select_date) values " +
        "(?, ?, ?)";

    public DemonStockMrTextToDB(String dateStr, JdbcTemplate jdbcTemplate, String path) {
        super(dateStr, jdbcTemplate, path);
    }

    @Override
    String getFileTag()
    {
        return "demon";
    }

    @Override
    public int insertDB(List<DemonStock> result)
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
            return result.size();
        }
        return 0;
    }

    @Override
    public DemonStock lineToObject(String line)
    {
        if (StringUtils.isEmpty(line))
        {
            return null;
        }
        String[] split = line.split("\t");
        if (null != split && split.length > 2)
        {
            DemonStock demonStock = new DemonStock(date);
            demonStock.setNum(split[0]);
            demonStock.setName(split[1]);
            return demonStock;
        }
        return null;
    }
}
