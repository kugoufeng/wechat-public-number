package cn.jeremy.wechat.entity;

import java.util.Date;
import lombok.Data;

@Data
public class WxMpUser
{
    private Integer id;

    private Boolean subscribe;

    private String openId;

    private String nickname;

    private String sexDesc;

    private Integer sex;

    private String language;

    private String city;

    private String province;

    private String country;

    private String headImgUrl;

    private Date subscribeTime;

    private String unionId;

    private String remark;

    private Integer groupId;

    private Long[] tagIds;

    private String subscribeScene;

    private String qrScene;

    private String qrSceneStr;

    public WxMpUser()
    {
    }

    public WxMpUser(me.chanjar.weixin.mp.bean.result.WxMpUser wxMpUser)
    {
        this.subscribe = wxMpUser.getSubscribe();
        this.openId = wxMpUser.getOpenId();
        this.nickname = wxMpUser.getNickname();
        this.sex = wxMpUser.getSex();
        this.language = wxMpUser.getLanguage();
        this.city = wxMpUser.getCity();
        this.province = wxMpUser.getProvince();
        this.country = wxMpUser.getCountry();
        this.headImgUrl = wxMpUser.getHeadImgUrl();
        if (wxMpUser.getSubscribeTime() != null)
        {
            this.subscribeTime = new Date(wxMpUser.getSubscribeTime() * 1000);
        }
        this.unionId = wxMpUser.getUnionId();
        this.remark = wxMpUser.getRemark();
        this.groupId = wxMpUser.getGroupId();
        this.tagIds = wxMpUser.getTagIds();
        this.subscribeScene = wxMpUser.getSubscribeScene();
        this.qrScene = wxMpUser.getQrScene();
        this.qrSceneStr = wxMpUser.getQrSceneStr();
    }
}
