package cn.jeremy.wechat.handler;

import cn.jeremy.wechat.entity.WxMpUser;
import cn.jeremy.wechat.service.WxMpUserService;
import me.chanjar.weixin.common.session.WxSessionManager;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.message.WxMpXmlMessage;
import me.chanjar.weixin.mp.bean.message.WxMpXmlOutMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Binary Wang(https://github.com/binarywang)
 */
@Component
public class UnsubscribeHandler extends AbstractHandler {

    @Autowired
    WxMpUserService wxMpUserService;

    @Override
    public WxMpXmlOutMessage handle(WxMpXmlMessage wxMessage,
                                    Map<String, Object> context, WxMpService wxMpService,
                                    WxSessionManager sessionManager) {
        String openId = wxMessage.getFromUser();
        String unionId = wxMessage.getUnionId();
        this.logger.info("取消关注用户 OPENID: " + openId + ", UNIONID: " + unionId);
        // 更新本地数据库为取消关注状态
        WxMpUser wxMpUser = wxMpUserService.selectByOpenIdAndUnionId(openId, unionId);
        if (null != wxMpUser)
        {
            wxMpUser.setSubscribe(false);
            wxMpUserService.update(wxMpUser);
        }
        return null;
    }

}
