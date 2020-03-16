package cn.jeremy.wechat.controller;

import cn.jeremy.common.utils.FileUtil;
import cn.jeremy.common.utils.ZipUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.http.HttpServletResponse;
import net.lingala.zip4j.exception.ZipException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/common")
public class CommonController
{
    @Value("${file.download.basepath}")
    private String basePath;

    @RequestMapping("/downloadFile")
    @ResponseBody
    public String downloadFile(HttpServletResponse response, @RequestParam("fileName") String fileName)
        throws ZipException
    {
        String file = basePath.concat(fileName);
        if (!FileUtil.isFileExists(file))
        {
            return "文件或者文件夹不存在";
        }
        boolean isZip = false;
        if (FileUtil.isDir(file))
        {
            ZipUtil.zipFolder(file);
            file = file.concat(".zip");
            fileName = fileName.concat(".zip");
            isZip = true;
        }

        response.reset();
        response.setHeader("Content-Disposition", "attachment;fileName=" + fileName);
        InputStream is = null;
        OutputStream os = null;
        try
        {
            is = new FileInputStream(file);
            os = response.getOutputStream();

            byte[] buff = new byte[1024];
            int len = -1;
            while ((len = is.read(buff)) > 0)
            {
                os.write(buff, 0, len);
            }
            os.flush();
        }
        catch (Exception e)
        {
            return "下载异常";
        }
        finally
        {
            FileUtil.closeIO(is, os);
            //删除压缩文件
            if (isZip)
            {
                FileUtil.deleteFile(file);
            }
        }

        return "成功";
    }

    @PostMapping(value = "/upload")
    public String upload(@RequestParam("file") MultipartFile file)
        throws IOException
    {
        String originalFilename = file.getOriginalFilename();
        String outPath = basePath + originalFilename;
        file.transferTo(new File(outPath));
        if (originalFilename.endsWith(".zip"))
        {
            ZipUtil.unZipFolder(outPath, true);
        }
        return "success";
    }

}
