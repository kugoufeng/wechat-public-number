package cn.jeremy.wechat.text;

import cn.jeremy.common.utils.DateTools;
import cn.jeremy.common.utils.FileUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

@Slf4j
public abstract class BaseMrTextToDB<T> implements TextToDB<T> {

    String dateStr;

    Date date;

    JdbcTemplate jdbcTemplate;

    String path;

    static final String FILE_NAME = "part-r-00000";

    abstract String getFileTag();

    public BaseMrTextToDB(String dateStr, JdbcTemplate jdbcTemplate, String path) {
        this.dateStr = dateStr;
        this.jdbcTemplate = jdbcTemplate;
        this.path = path;
        this.date = DateTools.timeStr2Date(dateStr, DateTools.DATE_FORMAT_10);
    }

    public FilenameFilter createFilenameFilter() {
        return new FilenameFilter()
        {
            @Override
            public boolean accept(File parentFile, String fileName)
            {
                File grandParentFile = null;
                if ((grandParentFile = parentFile.getParentFile()) != null)
                {
                    return dateStr.equals(grandParentFile.getName()) && getFileTag().equals(parentFile.getName()) &&
                            fileName.equals(FILE_NAME);
                }
                return false;
            }
        };
    }

    @Override
    public List<File> getFilesFromPath() {
        return FileUtil.listFilesInDirWithFilter(path, createFilenameFilter(), true);
    }

    @Override
    public List<T> filesToObjectList(List<File> files) {
        if (!CollectionUtils.isEmpty(files)) {
            List<T> result = new ArrayList<>();
            files.forEach(file -> {
                List<String> lines = FileUtil.readFile2List(file, "utf-8");
                if (!CollectionUtils.isEmpty(lines)) {
                    lines.forEach(line -> {
                        T t = lineToObject(line);
                        if (null != t) {
                            result.add(t);
                        }
                    });
                }
            });
            return result;
        }
        return null;
    }

    /**
     * 组装将数据存入数据库的过程
     *
     * @return
     * @throws
     * @author fengjiangtao
     */
    public int execInsertDB() {
        log.info("begin execInsertDB()");
        List<File> files = getFilesFromPath();
        List<T> tList = filesToObjectList(files);
        int count = insertDB(tList);
        log.info("end execInsertDB()");
        return count;
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
    <T> T queryForObject(String sql, Object[] args, Class<T> requiredType)
            throws DataAccessException {
        try {
            return jdbcTemplate.queryForObject(sql, args, requiredType);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }
}
