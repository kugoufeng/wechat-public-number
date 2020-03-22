package cn.jeremy.wechat.handler;

import cn.jeremy.wechat.builder.ImageBuilder;
import cn.jeremy.wechat.builder.TextBuilder;
import cn.jeremy.wechat.entity.WxMpMedia;
import cn.jeremy.wechat.entity.WxMpUser;
import cn.jeremy.wechat.service.ApiAuthorityService;
import cn.jeremy.wechat.service.WxMpMediaService;
import cn.jeremy.wechat.service.WxMpUserService;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

import static me.chanjar.weixin.common.api.WxConsts.EventType;

/**
 * @author Binary Wang(https://github.com/binarywang)
 */
@Component
public class MenuHandler extends AbstractHandler
{

    @Autowired
    ApiAuthorityService apiAuthorityService;

    @Autowired
    WxMpUserService wxMpUserService;

    @Autowired
    WxMpMediaService wxMpMediaService;

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
                return new ImageBuilder().build(wxMpMedia.getMediaId(), wxMessage, weixinService);
            }
            else
            {
                return new TextBuilder().build("充值后可以查看最近推荐股票", wxMessage, weixinService);
            }
        }
        return null;
    }

}
