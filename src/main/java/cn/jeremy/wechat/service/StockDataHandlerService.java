package cn.jeremy.wechat.service;

import cn.jeremy.common.utils.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

@Service
public class StockDataHandlerService {

    @Value("${file.download.basepath}")
    private String basePath;

    private List<File> getDirList() {
       return FileUtil.listFilesInDirWithFilter(basePath, new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                return "demon".equals(file.getName()) && name.equals("part-00000-r");
            }
        }, true);
    }
}
