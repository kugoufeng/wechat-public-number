package cn.jeremy.wechat.service;

import cn.jeremy.common.utils.DateTools;
import cn.jeremy.common.utils.FileUtil;
import cn.jeremy.wechat.stock.bean.StockCloseData;
import java.util.ArrayList;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class StockDataHandlerService
{

    @Value("${file.download.basepath}")
    private String basePath;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final String FILE_NAME = "part-r-00000";

    @Scheduled(cron = "0 34 23 ? * MON-FRI")
    public void importData()
    {
        importDemonData();
    }

    public void importDemonData()
    {
        String tag = "demon";
        List<File> files = getDirList(tag);
        List<StockCloseData> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(files))
        {
            for (File file : files)
            {
                String path = file.getPath();
                Date date = separateDateFromPath(path, tag);
                List<String> lines = FileUtil.readFile2List(file, "utf-8");
                if (!CollectionUtils.isEmpty(lines))
                {
                    for (String line : lines)
                    {
                        if (StringUtils.isEmpty(line))
                        {
                            continue;
                        }
                        String[] split = line.split("\t");
                        StockCloseData stockCloseData = new StockCloseData(date);
                        stockCloseData.setNum(split[0]);
                        stockCloseData.setName(split[1]);
                        result.add(stockCloseData);
                    }
                }
            }
        }
        if (!CollectionUtils.isEmpty(result))
        {
            String selectSql = "select count(1) from demon_stock where num = ? and Date(select_date) = ?";

            for (StockCloseData stockCloseData : result)
            {
                Integer count = queryForObject(selectSql,
                    new Object[] {stockCloseData.getNum(), stockCloseData.getToday()},
                    Integer.class);
                if (count == null || count == 0)
                {
                    jdbcTemplate.update("insert into demon_stock (num, name, select_date) values (?, ?, ?)",
                        new Object[] {stockCloseData.getNum(), stockCloseData.getName(), stockCloseData.getToday()});
                }
            }
        }
    }

    private List<File> getDirList(String tag)
    {
        return FileUtil.listFilesInDirWithFilter(basePath, new FilenameFilter()
        {
            @Override
            public boolean accept(File file, String name)
            {
                return tag.equals(file.getName()) && name.equals(FILE_NAME);
            }
        }, true);
    }

    private Date separateDateFromPath(String basePath, String tag)
    {
        String xg = basePath.lastIndexOf("/") > 0 ? "/" : "\\";
        basePath = basePath.substring(0, basePath.indexOf(xg.concat("demon")));
        String date = basePath.substring(basePath.lastIndexOf(xg) + 1);
        return DateTools.timeStr2Date(date, DateTools.DATE_FORMAT_10);
    }

    private <T> T queryForObject(String sql, Object[] args, Class<T> requiredType)
        throws DataAccessException
    {
        try
        {
            return jdbcTemplate.queryForObject(sql, args, requiredType);
        }
        catch (EmptyResultDataAccessException e)
        {
            return null;
        }
    }

}
