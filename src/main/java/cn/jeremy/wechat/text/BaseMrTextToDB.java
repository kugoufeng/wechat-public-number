package cn.jeremy.wechat.text;

import cn.jeremy.common.utils.FileUtil;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

public abstract class BaseMrTextToDB<T> implements TextToDB<T>
{
    JdbcTemplate jdbcTemplate;

    String path;

    abstract String getFileTag();

    abstract FilenameFilter createFilenameFilter();

    @Override
    public List<File> getFilesFromPath()
    {
        return FileUtil.listFilesInDirWithFilter(path, createFilenameFilter(), true);
    }

    @Override
    public List<T> filesToObjectList(List<File> files)
    {
        if (!CollectionUtils.isEmpty(files))
        {
            List<T> result = new ArrayList<>();
            files.forEach(file -> {
                List<String> lines = FileUtil.readFile2List(file, "utf-8");
                if (!CollectionUtils.isEmpty(lines))
                {
                    lines.forEach(line -> {
                        T t = lineToObject(line);
                        if (null != t)
                        {
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
    public void execInsertDB()
    {
        List<File> files = getFilesFromPath();
        List<T> tList = filesToObjectList(files);
        insertDB(tList);
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
