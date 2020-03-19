package cn.jeremy.wechat.service;

import cn.jeremy.common.utils.DateTools;
import cn.jeremy.common.utils.FileUtil;
import cn.jeremy.wechat.stock.bean.SelectStockData;
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

    @Scheduled(cron = "0 34 23 ? * MON-FRI")
    public void importData() {
        importDemonData();
    }

    /**
     * 导入xxx/demo/FILE_NAME 文本中的数据到数据库中
     */
    public void importDemonData() {
        List<File> files = getDirList("demon");
        List<SelectStockData> result = getDemonStokcDataFromFile(files);
        insertDemonStock(result);
    }

    public void importDemonData(String date) {
        List<File> files = getDirList("demon", date);
        List<SelectStockData> result = getDemonStokcDataFromFile(files);
        insertDemonStock(result);
    }

    private List<SelectStockData> getDemonStokcDataFromFile(List<File> files) {
        if (!CollectionUtils.isEmpty(files)) {
            List<SelectStockData> result = new ArrayList<>();
            for (File file : files) {
                Date date = separateDateFromPath(file);
                List<String> lines = FileUtil.readFile2List(file, "utf-8");
                if (!CollectionUtils.isEmpty(lines)) {
                    for (String line : lines) {
                        if (StringUtils.isEmpty(line)) {
                            continue;
                        }
                        String[] split = line.split("\t");
                        SelectStockData selectStockData = new SelectStockData(date);
                        selectStockData.setNum(split[0]);
                        selectStockData.setName(split[1]);
                        result.add(selectStockData);
                    }
                }
            }
            return result;
        }
        return null;
    }


    private void insertDemonStock(List<SelectStockData> result) {
        if (!CollectionUtils.isEmpty(result)) {
            result.forEach(s -> {
                Integer count = queryForObject(COUNT_FROM_DEMON_STOCK_SQL,
                        new Object[]{s.getNum(), s.getSelectDate()},
                        Integer.class);
                if (count == null || count == 0) {
                    jdbcTemplate.update(INSERT_TO_DEMON_STOCK_SQL,
                            new Object[]{s.getNum(), s.getName(),
                                    s.getSelectDate()});
                }
            });
        }
    }

    /**
     * 根据目录tag，分离出符合条件的文件
     *
     * @param tag
     * @return
     */
    private List<File> getDirList(String tag) {
        return FileUtil.listFilesInDirWithFilter(basePath, new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return tag.equals(file.getName()) && name.equals(FILE_NAME);
            }
        }, true);
    }

    /**
     * 根据目录tag，分离出符合条件的文件
     *
     * @param tag
     * @return
     */
    private List<File> getDirList(String tag, String date) {
        return FileUtil.listFilesInDirWithFilter(basePath, new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                File parentFile = null;
                if ((parentFile = file.getParentFile()) != null) {
                    return date.equals(parentFile.getName()) && tag.equals(file.getName()) && name.equals(FILE_NAME);
                }
                return false;
            }
        }, true);
    }

    /**
     * 从路径中分隔出日期 ，路径格式/xx/xx/date/tag/xxx
     *
     * @param file
     * @return
     */
    private Date separateDateFromPath(File file) {
        String date = file.getParentFile().getParentFile().getName();
        return DateTools.timeStr2Date(date, DateTools.DATE_FORMAT_10);
    }

    /**
     * 重写jdbcTemplate的queryForObject方法，避免查询为空直接报错的异常
     *
     * @param sql
     * @param args
     * @param requiredType
     * @param <T>
     * @return
     * @throws DataAccessException
     */
    private <T> T queryForObject(String sql, Object[] args, Class<T> requiredType)
            throws DataAccessException {
        try {
            return jdbcTemplate.queryForObject(sql, args, requiredType);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

}
