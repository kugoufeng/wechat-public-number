package cn.jeremy.wechat.service;

import cn.jeremy.common.utils.DateTools;
import cn.jeremy.common.utils.FileUtil;
import cn.jeremy.wechat.bean.DemonStockHistory;
import cn.jeremy.wechat.bean.DemonStockHistory.DemonStockBuyElement;
import cn.jeremy.wechat.bean.DemonStockHistory.DemonStockSellElement;
import cn.jeremy.wechat.bean.TodayBuyPositions;
import cn.jeremy.wechat.entity.DemonStock;
import cn.jeremy.wechat.entity.StockCloseData;
import cn.jeremy.wechat.entity.WxMpMedia;
import cn.jeremy.wechat.stock.ThsMockTrade;
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
import me.chanjar.weixin.mp.bean.material.WxMpMaterial;
import me.chanjar.weixin.mp.bean.material.WxMpMaterialUploadResult;
import org.apache.commons.lang3.StringUtils;
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

    private final JdbcTemplate jdbcTemplate;

    private final PhantomjsService phantomjsService;

    private final WxMpService wxService;

    private final WxMpMediaService wxMpMediaService;

    private final DemonStockService demonStockService;

    private final StockCloseService stockCloseService;

    private final ThsMockTrade thsMockTrade;

    public StockDataHandlerService(JdbcTemplate jdbcTemplate, PhantomjsService phantomjsService,
        WxMpService wxService, WxMpMediaService wxMpMediaService,
        DemonStockService demonStockService, StockCloseService stockCloseService, ThsMockTrade thsMockTrade)
    {
        this.jdbcTemplate = jdbcTemplate;
        this.phantomjsService = phantomjsService;
        this.wxService = wxService;
        this.wxMpMediaService = wxMpMediaService;
        this.demonStockService = demonStockService;
        this.stockCloseService = stockCloseService;
        this.thsMockTrade = thsMockTrade;
    }

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
        uploadDemonStockHistoryMedia();
    }

    /**
     * 生成今日操作策略
     *
     * @return
     * @throws
     * @author fengjiangtao
     */
    @Scheduled(cron = "0 27 9 ? * MON-FRI")
    public void genStockPositionsPic()
    {
        String mediaName = "buy_stock_positions";
        List<DemonStock> demonStocks = demonStockService.queryNearestDemonStock();
        if (!CollectionUtils.isEmpty(demonStocks))
        {
            Date maxDate = stockCloseService.selectMaxDate();
            Date selectDate = demonStocks.get(0).getSelectDate();
            //不是上一个交易日选出的股票无操作策略
            if (maxDate.compareTo(selectDate) == 0)
            {
                List<TodayBuyPositions> todayBuyPositionsList = new ArrayList<>();
                for (DemonStock demonStock : demonStocks)
                {
                    StockCloseData stockCloseData = thsMockTrade.getStockCloseData(demonStock.getNum());
                    if (null == stockCloseData)
                    {
                        continue;
                    }
                    TodayBuyPositions todayBuyPositions =
                        new TodayBuyPositions(demonStock.getName(),
                            demonStock.getNum(),
                            demonStock.getSelectDate(),
                            stockCloseData.getOpenPrice());
                    todayBuyPositionsList.add(todayBuyPositions);
                }
                uploadStockPositionsMedia(todayBuyPositionsList, mediaName);
            }
        }
    }

    /**
     * 上传上个月的妖股历史图片到微信永久素材中
     *
     * @param list
     * @return
     * @throws
     * @author fengjiangtao
     */
    private void uploadStockPositionsMedia(List<TodayBuyPositions> list, String mediaName)
    {
        String filePath = genTodayBuyPositionsPic(list);
        if (null != filePath)
        {
            File file = new File(filePath);
            try
            {
                WxMediaUploadResult result = wxService.getMaterialService().mediaUpload("image", file);
                if (null != result)
                {
                    WxMpMedia wxMpMedia = new WxMpMedia();
                    wxMpMedia.setName(mediaName);
                    wxMpMedia.setType(result.getType());
                    wxMpMedia.setMediaId(result.getMediaId());
                    wxMpMedia.setCreateTime(new Date(result.getCreatedAt() * 1000));
                    wxMpMedia.setExpireTime(new Date(DateTools.addDate(result.getCreatedAt() * 1000,
                        3,
                        DateTools.DAY)));
                    wxMpMediaService.insert(wxMpMedia);
                }
            }
            catch (WxErrorException e)
            {
                log.error("uploadStockPositionsMedia has error, e:{}", e);
            }
            finally
            {
                FileUtil.deleteFile(file);
            }
        }
    }

    /**
     * 删除过期的数据
     *
     * @return
     * @throws
     * @author fengjiangtao
     */
    @Scheduled(cron = "0 0 15 ? * MON-FRI")
    public void delExpireData()
    {
        wxMpMediaService.delByName("buy_stock_positions");
    }

    /**
     * 统计当月或者其它月份推荐的妖股数据
     *
     * @param monthOffset
     * @return java.util.List<cn.jeremy.wechat.bean.DemonStockHistory>
     * @throws
     * @author fengjiangtao
     */
    public List<DemonStockHistory> queryDemonStockHistory(int monthOffset)
    {
        List<DemonStock> demonStocks;
        if (monthOffset >= 0)
        {
            demonStocks = demonStockService.queryCurrentMonthData();
        }
        else
        {
            demonStocks = demonStockService.queryOtherMonthData(monthOffset);
        }
        if (CollectionUtils.isEmpty(demonStocks))
        {
            return null;
        }
        List<DemonStockHistory> result = new ArrayList<>();
        demonStocks.forEach(demonStock -> {
            List<StockCloseData> stockCloseDataList =
                stockCloseService.selectByDate(demonStock.getSelectDate(), demonStock.getNum(), demonStock.getName());
            if (CollectionUtils.isEmpty(stockCloseDataList) || stockCloseDataList.size() < 2)
            {
                return;
            }
            StockCloseData stockCloseData = stockCloseDataList.get(0);
            DemonStockHistory demonStockHistory =
                new DemonStockHistory(demonStock.getName(), demonStock.getNum(), demonStock.getSelectDate());
            DemonStockBuyElement demonStockBuyElement = new DemonStockBuyElement(stockCloseData.getOpenPrice(),
                stockCloseData.getTopPrice(),
                stockCloseData.getLowPrice(),
                stockCloseData.getClosePrice(),
                stockCloseData.getChg());
            demonStockHistory.setDemonStockBuyElement(demonStockBuyElement);
            stockCloseDataList.remove(stockCloseData);
            List<DemonStockSellElement> demonStockSellElements = new ArrayList<>(stockCloseDataList.size());
            for (StockCloseData closeData : stockCloseDataList)
            {
                DemonStockSellElement demonStockSellElement = new DemonStockSellElement(closeData.getOpenPrice(),
                    closeData.getTopPrice(),
                    closeData.getLowPrice(),
                    closeData.getClosePrice(),
                    demonStockBuyElement.getAvgPrice(),
                    demonStockBuyElement.getTotalPositions());
                demonStockSellElements.add(demonStockSellElement);
            }
            demonStockHistory.setDemonStockSellElements(demonStockSellElements);
            result.add(demonStockHistory);
        });
        return result;
    }

    /**
     * 上传demon-stock-history图片到微信服务号的临时素材中 如果当前月已经过了10天，则上传上个月的图片到永久素材中，不在更新
     *
     * @return
     * @throws
     * @author fengjiangtao
     */
    public void uploadDemonStockHistoryMedia()
    {
        Date date = new Date();
        String base_media_name = "demon-stock-history-";
        int dayOfMonth = DateTools.getDayOfMonth(date);
        String lastMonth = DateTools.addDate(date.getTime(), -1, DateTools.MONTH, DateTools.DATE_FORMAT_7);
        String lastMonthName = base_media_name.concat(lastMonth);
        String currentMonth = DateTools.format(date, DateTools.DATE_FORMAT_7);
        String currentMonthName = base_media_name.concat(currentMonth);
        if (dayOfMonth >= 10)
        {
            uploadDemonStockHistoryMaterial(lastMonthName);
        }
        else
        {
            uploadDemonStockHistoryMedia(-1, lastMonthName);
        }

        uploadDemonStockHistoryMedia(0, currentMonthName);
    }

    /**
     * 上传某个月的妖股历史图片到微信临时素材中
     *
     * @param monthOffset
     * @param mediaName
     * @return
     * @throws
     * @author fengjiangtao
     */
    private void uploadDemonStockHistoryMedia(int monthOffset, String mediaName)
    {
        List<DemonStockHistory> demonStockHistories = queryDemonStockHistory(monthOffset);
        String filePath = genDemonStockHistoryPic(demonStockHistories);
        if (null != filePath)
        {
            File file = new File(filePath);
            try
            {
                WxMediaUploadResult result = wxService.getMaterialService().mediaUpload("image", file);
                if (null != result)
                {
                    WxMpMedia wxMpMedia = new WxMpMedia();
                    wxMpMedia.setName(mediaName);
                    wxMpMedia.setType(result.getType());
                    wxMpMedia.setMediaId(result.getMediaId());
                    wxMpMedia.setCreateTime(new Date(result.getCreatedAt() * 1000));
                    wxMpMedia.setExpireTime(new Date(DateTools.addDate(result.getCreatedAt() * 1000,
                        3,
                        DateTools.DAY)));
                    wxMpMediaService.insert(wxMpMedia);
                }
            }
            catch (WxErrorException e)
            {
                log.error("uploadDemonStockHistoryMedia has error, e:{}", e);
            }
            finally
            {
                FileUtil.deleteFile(file);
            }
        }
    }

    /**
     * 上传上个月的妖股历史图片到微信永久素材中
     *
     * @param lastMonthName
     * @return
     * @throws
     * @author fengjiangtao
     */
    private void uploadDemonStockHistoryMaterial(String lastMonthName)
    {
        WxMpMedia wxMpMedia = wxMpMediaService.queryByName(lastMonthName);
        //不是永久素材
        if (null != wxMpMedia && wxMpMedia.getExpireTime() != null)
        {
            List<DemonStockHistory> demonStockHistories = queryDemonStockHistory(-1);
            String filePath = genDemonStockHistoryPic(demonStockHistories);
            if (null != filePath)
            {
                File file = new File(filePath);
                WxMpMaterial wxMaterial = new WxMpMaterial();
                wxMaterial.setFile(file);
                wxMaterial.setName(file.getName());
                try
                {
                    WxMpMaterialUploadResult result =
                        wxService.getMaterialService().materialFileUpload("image", wxMaterial);
                    wxMpMedia.setMediaId(result.getMediaId());
                    wxMpMedia.setCreateTime(new Date());
                    wxMpMediaService.updateById(wxMpMedia);
                }
                catch (WxErrorException e)
                {
                    log.error("uploadDemonStockHistoryMaterial has error, e:{}", e);
                }
                finally
                {
                    FileUtil.deleteFile(file);
                }
            }
        }
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
                    wxMpMedia.setCreateTime(new Date(result.getCreatedAt() * 1000));
                    wxMpMedia.setExpireTime(new Date(DateTools.addDate(result.getCreatedAt() * 1000,
                        3,
                        DateTools.DAY)));
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
            return phantomjsService.genPic("demon-stock", 750, (result.size() + 1) * 50 + 210, params);
        }
        return null;
    }

    /**
     * 生成图片
     *
     * @param result
     * @return
     */
    public String genDemonStockHistoryPic(List<DemonStockHistory> result)
    {
        if (!CollectionUtils.isEmpty(result))
        {
            Map<String, String> params = new HashMap<>();
            params.put("data", JSONObject.toJSONString(result));
            return phantomjsService.genPic("demon-stock-history", 750, (result.size() + 1) * 50 + 210, params);
        }
        return null;
    }

    /**
     * 生成图片
     *
     * @param result
     * @return
     */
    public String genTodayBuyPositionsPic(List<TodayBuyPositions> result)
    {
        if (!CollectionUtils.isEmpty(result))
        {
            Map<String, String> params = new HashMap<>();
            params.put("data", JSONObject.toJSONString(result));
            return phantomjsService.genPic("today-buy-positions", 750, (result.size() + 1) * 50 + 210, params);
        }
        return null;
    }

}
