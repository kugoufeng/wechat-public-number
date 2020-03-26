package cn.jeremy.wechat.handler;

import static me.chanjar.weixin.common.api.WxConsts.XmlMsgType;

import cn.jeremy.wechat.builder.TextBuilder;
import cn.jeremy.wechat.service.StockBaseService;
import cn.jeremy.wechat.stock.ThsMockTrade;
import cn.jeremy.wechat.entity.StockCloseData;
import java.util.Map;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Binary Wang(https://github.com/binarywang)
 */
@Component
public class MsgHandler extends AbstractHandler
{

    @Autowired
    StockBaseService stockBaseService;

    @Autowired
    ThsMockTrade thsMockTrade;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
        Map<String, Object> context, WxMpService weixinService,
        WxSessionManager sessionManager)
    {

        if (!wxMessage.getMsgType().equals(XmlMsgType.EVENT))
        {
            //TODO 可以选择将消息保存到本地
        }

        String content = wxMessage.getContent();
        if (content.length() == 6)
        {
            String name = stockBaseService.selectNameByNum(content);
            if (StringUtils.isNotEmpty(name))
            {
                StockCloseData stockCloseData = thsMockTrade.getStockCloseData(content);
                if (null != stockCloseData)
                {
                    return new TextBuilder().build(stockCloseData.getClosePrice() + "|" + stockCloseData.getChg(),
                        wxMessage,
                        weixinService);
                }
            }
        }

        if (StringUtils.isNotEmpty(content))
        {
            String num = stockBaseService.selectNumByName(content);
            if (StringUtils.isNotEmpty(num))
            {
                StockCloseData stockCloseData = thsMockTrade.getStockCloseData(num);
                if (null != stockCloseData)
                {
                    return new TextBuilder().build(stockCloseData.getClosePrice() + "|" + stockCloseData.getChg(),
                        wxMessage,
                        weixinService);
                }
            }
        }

        return new TextBuilder().build(content, wxMessage, weixinService);

    }

}
