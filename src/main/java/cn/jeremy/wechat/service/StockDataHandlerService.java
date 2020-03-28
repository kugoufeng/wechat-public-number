package cn.jeremy.wechat.service;

import cn.jeremy.common.utils.DateTools;
import cn.jeremy.common.utils.FileUtil;
import cn.jeremy.wechat.entity.DemonStock;
import cn.jeremy.wechat.entity.WxMpMedia;
import cn.jeremy.wechat.text.ContinuousStockFundMrTextToDB;
import cn.jeremy.wechat.text.DemonStockMrTextToDB;

import com.alibaba.fastjson.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 将文本文件内容导入数据库中
 */
@Service
@Slf4j
public class StockDataHandlerService
{

    @Value("${file.download.basepath}")
    private String basePath;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    PhantomjsService phantomjsService;

    @Autowired
    WxMpService wxService;

    @Autowired
    WxMpMediaService wxMpMediaService;

    @Autowired
    DemonStockService demonStockService;

    private static final String FILE_NAME = "part-r-00000";



    @Scheduled(cron = "0 35 18 ? * MON-FRI")
    public void importData()
    {
        String date = DateTools.date2TimeStr(new Date(), DateTools.DATE_FORMAT_10);
        int count = new DemonStockMrTextToDB(date, jdbcTemplate, basePath).execInsertDB();
        if (count > 0)
        {
            uploadDemonStockMedia();
        }
        new ContinuousStockFundMrTextToDB(date, jdbcTemplate, basePath).execInsertDB();
    }

    /**
     * 上传demon-stock图片到微信服务号的临时素材中
     *
     * @return
     * @throws
     * @author fengjiangtao
     */
    public void uploadDemonStockMedia()
    {
        List<DemonStock> demonStocks = demonStockService.queryNearestDemonStock();
        String filePath = genDemonStockPic(demonStocks);
        if (StringUtils.isNotEmpty(filePath))
        {
            File file = new File(filePath);
            try
            {
                WxMediaUploadResult result = wxService.getMaterialService().mediaUpload("image", file);
                if (null != result)
                {
                    WxMpMedia wxMpMedia = new WxMpMedia();
                    wxMpMedia.setName("nearest-demon-stock");
                    wxMpMedia.setType(result.getType());
                    wxMpMedia.setMediaId(result.getMediaId());
                    wxMpMedia.setCreateTime(new Date(result.getCreatedAt()*1000));
                    wxMpMedia.setExpireTime(new Date(DateTools.addDate(result.getCreatedAt()*1000, 3, DateTools.DAY)));
                    wxMpMediaService.insert(wxMpMedia);
                }
            }
            catch (WxErrorException e)
            {
                log.error("uploadDemonStockMedia has error, e:{}", e);
            }
            finally
            {
                FileUtil.deleteFile(filePath);
            }
        }

    }



    /**
     * 生成图片
     *
     * @param result
     * @return
     */
    public String genDemonStockPic(List<DemonStock> result)
    {
        if (!CollectionUtils.isEmpty(result))
        {
            Map<String, String> params = new HashMap<>();
            params.put("data", JSONObject.toJSONString(result));
            return phantomjsService.genPic("demon-stock", 750, result.size() * 50 + 90, params);
        }
        return null;
    }

}
