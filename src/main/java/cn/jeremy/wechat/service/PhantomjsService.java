package cn.jeremy.wechat.service;

import cn.jeremy.common.utils.HttpTools;
import cn.jeremy.common.utils.bean.HttpResult;
import com.alibaba.fastjson.JSONObject;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.httpclient.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PhantomjsService
{
    @Value("${temp.path}")
    private String tempPath;

    @Value("${phantomjs.service.url}")
    private String phantomjsServiceUrl;

    /**
     * 生成图片，并将图片保存在临时目录
     *
     * @param htmlTag
     * @param width
     * @param height
     * @param params
     * @return java.lang.String
     * @throws
     * @author fengjiangtao
     */
    public String genPic(String htmlTag, String width, String height, Map<String, String> params)
    {
        String fileName = UUID.randomUUID().toString().concat(".jpg");
        String filePath = tempPath.concat(fileName);
        String url = phantomjsServiceUrl.replace("{htmlTag}", htmlTag)
            .replace("{fileName}", fileName)
            .replace("{width}", String.valueOf(width))
            .replace("{height}", String.valueOf(height));
        HttpResult httpResult =
            HttpTools.getInstance().downFileByPost(url, filePath, "utf-8", JSONObject.toJSONString(params));
        if (httpResult.getRespCode() == HttpStatus.SC_OK)
        {
            return filePath;
        }
        return null;
    }
}
