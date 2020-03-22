package cn.jeremy.wechat.service;

import cn.jeremy.wechat.entity.WxMpUser;
import java.util.Date;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class WxMpUserService
{
    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final String INSERT =
        "insert into wx_mp_user (subscribe, open_id, nickname, sex, language, city, province, country, head_img_url, " +
            "subscribe_time, union_id, remark, group_id, tag_ids, subscribe_scene, qr_scene, qr_scene_str) values (?," +
            " ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SELECT_BY_OPENID_AND_UNION_ID =
        "select * from wx_mp_user where open_id = ? and union_id = ?";

    private static final String SELECT_BY_OPENID=
        "select * from wx_mp_user where open_id = ?";

    private static final String UPDATE_BY_ID =
        "update wx_mp_user set subscribe = ?, open_id = ?, nickname = ?, sex = ?, language = ?, " +
            "city = ?, province = ?, country = ?, head_img_url = ?, subscribe_time = ?, union_id = ?, remark = ?, " +
            "group_id = ?, tag_ids = ?, subscribe_scene = ?, qr_scene = ?, qr_scene_str = ? where id = ?";

    public WxMpUser selectByOpenIdAndUnionId(String openId, String unionId)
    {
        WxMpUser wxMpUser = new WxMpUser();
        String sql = SELECT_BY_OPENID_AND_UNION_ID;
        Object[] objects = new Object[] {openId, unionId};
        if (StringUtils.isEmpty(unionId))
        {
            sql = SELECT_BY_OPENID;
            objects = new Object[] {openId};
        }
        jdbcTemplate.query(sql, objects, resultSet -> {
            wxMpUser.setId(resultSet.getInt(1));
            wxMpUser.setSubscribe(resultSet.getBoolean(2));
            wxMpUser.setOpenId(resultSet.getString(3));
            wxMpUser.setNickname(resultSet.getString(4));
            wxMpUser.setSex(resultSet.getInt(5));
            wxMpUser.setLanguage(resultSet.getString(6));
            wxMpUser.setCity(resultSet.getString(7));
            wxMpUser.setProvince(resultSet.getString(8));
            wxMpUser.setCountry(resultSet.getString(9));
            wxMpUser.setHeadImgUrl(resultSet.getString(10));
            wxMpUser.setSubscribeTime(new Date(resultSet.getDate(11).getTime()));
            wxMpUser.setUnionId(resultSet.getString(12));
            wxMpUser.setRemark(resultSet.getString(13));
            wxMpUser.setGroupId(resultSet.getInt(14));
            String tagIdsStr = resultSet.getString(15);
            String[] split = StringUtils.split(tagIdsStr, ",");
            if (ArrayUtils.isNotEmpty(split))
            {
                Long[] tagIds = new Long[split.length];
                for (int i = 0; i < split.length; i++)
                {
                    tagIds[i] = Long.getLong(split[i]);
                }
                wxMpUser.setTagIds(tagIds);
            }
            wxMpUser.setSubscribeScene(resultSet.getString(16));
            wxMpUser.setQrScene(resultSet.getString(17));
            wxMpUser.setQrSceneStr(resultSet.getString(18));
        });
        if (wxMpUser.getId() != null)
        {
            return wxMpUser;
        }
        return null;
    }

    public int update(WxMpUser wxMpUser)
    {
        return jdbcTemplate.update(UPDATE_BY_ID,
            new Object[] {wxMpUser.getSubscribe(), wxMpUser.getOpenId(), wxMpUser.getNickname(), wxMpUser.getSex(),
                wxMpUser.getLanguage(), wxMpUser.getCity(), wxMpUser.getProvince(), wxMpUser.getCountry(),
                wxMpUser.getHeadImgUrl(), wxMpUser.getSubscribeTime(), wxMpUser.getUnionId(), wxMpUser.getRemark(),
                wxMpUser.getGroupId(), StringUtils.join(wxMpUser.getTagIds(), ","), wxMpUser.getSubscribeScene(),
                wxMpUser.getQrScene(), wxMpUser.getQrSceneStr(), wxMpUser.getId()});
    }

    public int insert(WxMpUser wxMpUser)
    {
        return jdbcTemplate.update(INSERT,
            new Object[] {wxMpUser.getSubscribe(), wxMpUser.getOpenId(), wxMpUser.getNickname(), wxMpUser.getSex(),
                wxMpUser.getLanguage(), wxMpUser.getCity(), wxMpUser.getProvince(), wxMpUser.getCountry(),
                wxMpUser.getHeadImgUrl(), wxMpUser.getSubscribeTime(), wxMpUser.getUnionId(), wxMpUser.getRemark(),
                wxMpUser.getGroupId(), StringUtils.join(wxMpUser.getTagIds(), ","), wxMpUser.getSubscribeScene(),
                wxMpUser.getQrScene(), wxMpUser.getQrSceneStr()});
    }

    public int insertOrUpdate(WxMpUser wxMpUser)
    {
        WxMpUser oldWxMpUser = selectByOpenIdAndUnionId(wxMpUser.getOpenId(), wxMpUser.getUnionId());
        if (null != oldWxMpUser)
        {
            wxMpUser.setId(oldWxMpUser.getId());
            return update(wxMpUser);
        }
        return insert(wxMpUser);
    }
}
