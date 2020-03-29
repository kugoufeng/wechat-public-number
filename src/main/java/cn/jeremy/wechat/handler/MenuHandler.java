package cn.jeremy.wechat.handler;

import static me.chanjar.weixin.common.api.WxConsts.EventType;

import cn.jeremy.common.utils.DateTools;
import cn.jeremy.wechat.builder.ImageBuilder;
import cn.jeremy.wechat.builder.TextBuilder;
import cn.jeremy.wechat.entity.WxMpMedia;
import cn.jeremy.wechat.entity.WxMpUser;
import cn.jeremy.wechat.service.ApiAuthorityService;
import cn.jeremy.wechat.service.StockDataHandlerService;
import cn.jeremy.wechat.service.WxMpMediaService;
import cn.jeremy.wechat.service.WxMpUserService;
import java.util.Date;
import java.util.Map;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Binary Wang(https://github.com/binarywang)
 */
@Component
public class MenuHandler extends AbstractHandler
{

   private final ApiAuthorityService apiAuthorityService;

    private final WxMpUserService wxMpUserService;

    private final WxMpMediaService wxMpMediaService;

    private final ObjectProvider<StockDataHandlerService> stockDataHandlerService;

    public MenuHandler(ApiAuthorityService apiAuthorityService, WxMpUserService wxMpUserService,
        WxMpMediaService wxMpMediaService, ObjectProvider<StockDataHandlerService> stockDataHandlerService)
    {
        this.apiAuthorityService = apiAuthorityService;
        this.wxMpUserService = wxMpUserService;
        this.wxMpMediaService = wxMpMediaService;
        this.stockDataHandlerService = stockDataHandlerService;
    }

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
        Map<String, Object> context, WxMpService weixinService,
        WxSessionManager sessionManager)
    {
        if (EventType.VIEW.equals(wxMessage.getEvent()))
        {
            return null;
        }
        String eventKey = wxMessage.getEventKey();
        String openId = wxMessage.getFromUser();
        String unionId = wxMessage.getUnionId();
        WxMpUser wxMpUser = wxMpUserService.selectByOpenIdAndUnionId(openId, unionId);
        if (null == wxMpUser)
        {
            return new TextBuilder().build("请重新关注", wxMessage, weixinService);
        }
        if ("demon_stock".equals(eventKey))
        {
            int line = apiAuthorityService.decrLeftNum(wxMpUser.getId(), 1);
            if (line == 1)
            {
                WxMpMedia wxMpMedia = wxMpMediaService.queryByName("nearest-demon-stock");
                if (null == wxMpMedia)
                {
                    return new TextBuilder().build("最近没有推荐", wxMessage, weixinService);
                }
                //已经过期, 更新推荐图片
                if (new Date().compareTo(wxMpMedia.getExpireTime()) >= 0)
                {
                    stockDataHandlerService.getIfAvailable().uploadDemonStockMedia();
                    wxMpMedia = wxMpMediaService.queryByName("nearest-demon-stock");
                }
                if (null == wxMpMedia)
                {
                    return new TextBuilder().build("最近没有推荐", wxMessage, weixinService);
                }
                return new ImageBuilder().build(wxMpMedia.getMediaId(), wxMessage, weixinService);
            }
            else
            {
                return new TextBuilder().build("充值后可以查看最近推荐股票", wxMessage, weixinService);
            }
        }
        else if ("demon_stock_history".equals(eventKey))
        {
            Date date = new Date();
            String base_media_name = "demon-stock-history-";
            String currentMonth = DateTools.format(date, DateTools.DATE_FORMAT_7);
            String currentMonthName = base_media_name.concat(currentMonth);
            WxMpMedia wxMpMedia = wxMpMediaService.queryByName(currentMonthName);
            if (null == wxMpMedia)
            {
                return new TextBuilder().build("没有数据", wxMessage, weixinService);
            }
            //已经过期, 更新推荐图片
            if (new Date().compareTo(wxMpMedia.getExpireTime()) >= 0)
            {
                stockDataHandlerService.getIfAvailable().uploadDemonStockHistoryMedia();
                wxMpMedia = wxMpMediaService.queryByName(currentMonthName);
            }
            if (null == wxMpMedia)
            {
                return new TextBuilder().build("没有数据", wxMessage, weixinService);
            }
            return new ImageBuilder().build(wxMpMedia.getMediaId(), wxMessage, weixinService);
        }
        else if ("demon_stock_history_last".equals(eventKey))
        {
            Date date = new Date();
            String base_media_name = "demon-stock-history-";
            String lastMonth = DateTools.addDate(date.getTime(), -1, DateTools.MONTH, DateTools.DATE_FORMAT_7);
            String lastMonthName = base_media_name.concat(lastMonth);
            WxMpMedia wxMpMedia = wxMpMediaService.queryByName(lastMonthName);
            if (null == wxMpMedia)
            {
                return new TextBuilder().build("没有数据", wxMessage, weixinService);
            }
            //已经过期, 更新推荐图片
            if (wxMpMedia.getExpireTime() != null && new Date().compareTo(wxMpMedia.getExpireTime()) >= 0)
            {
                stockDataHandlerService.getIfAvailable().uploadDemonStockHistoryMedia();
                wxMpMedia = wxMpMediaService.queryByName(lastMonthName);
            }
            if (null == wxMpMedia)
            {
                return new TextBuilder().build("没有数据", wxMessage, weixinService);
            }
            return new ImageBuilder().build(wxMpMedia.getMediaId(), wxMessage, weixinService);
        }
        else if ("stock_positions".equals(eventKey))
        {
            String name = "buy_stock_positions";
            WxMpMedia wxMpMedia = wxMpMediaService.queryByName(name);
            if (null == wxMpMedia)
            {
                return new TextBuilder().build("没有数据", wxMessage, weixinService);
            }
            return new ImageBuilder().build(wxMpMedia.getMediaId(), wxMessage, weixinService);
        }
        return null;
    }

}
