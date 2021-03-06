package cn.jeremy.wechat.text;

import java.io.File;
import java.util.List;

/**
 * 文本文件转数据库接口
 */
public interface TextToDB<T> {

    /**
     * 分离指定目录下特定的文件
     *
     * @return
     */
    public List<File> getFilesFromPath();

    /**
     * 将文件内容转换为对象
     *
     * @param files
     * @return
     */
    public List<T> filesToObjectList(List<File> files);

    /**
     * 将对象插入数据中
     *
     * @param list
     */
    public int insertDB(List<T> list);

    /**
     * 将行数据转换为对象
     *
     * @param line
     * @return
     */
    public T lineToObject(String line);
}
