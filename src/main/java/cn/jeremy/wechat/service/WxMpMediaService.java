package cn.jeremy.wechat.service;

import cn.jeremy.wechat.entity.WxMpMedia;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class WxMpMediaService
{
    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final String INSERT_SQL =
        "insert into wx_mp_media (name, type, media_id, create_time, expire_time) values (?,?,?,?,?)";

    private static final String DELETE_BY_NAME = "delete from wx_mp_media where name = ?";

    private static final String SELECT_BY_NAME = "select * from wx_mp_media where name = ?";

    private static final String UPDATE_BY_ID =
        "update wx_mp_media set name = ?, type = ?, media_id = ?, create_time = ?, expire_time = ? where id = ?";

    public int insert(WxMpMedia wxMpMedia)
    {
        jdbcTemplate.update(DELETE_BY_NAME, new Object[] {wxMpMedia.getName()});
        return jdbcTemplate.update(INSERT_SQL,
            new Object[] {wxMpMedia.getName(), wxMpMedia.getType(), wxMpMedia.getMediaId(), wxMpMedia.getCreateTime(),
                wxMpMedia.getExpireTime()});
    }

    public WxMpMedia queryByName(String name)
    {
        WxMpMedia wxMpMedia = new WxMpMedia();
        jdbcTemplate.query(SELECT_BY_NAME, new Object[] {name}, resultSet -> {
            wxMpMedia.setId(resultSet.getInt(1));
            wxMpMedia.setName(resultSet.getString(2));
            wxMpMedia.setType(resultSet.getString(3));
            wxMpMedia.setMediaId(resultSet.getString(4));
            wxMpMedia.setCreateTime(new Date(resultSet.getDate(5).getTime()));
            java.sql.Date expireTime = resultSet.getDate(6);
            if (null != expireTime)
            {
                wxMpMedia.setExpireTime(new Date(expireTime.getTime()));
            }
        });
        if (wxMpMedia.getId() != null)
        {
            return wxMpMedia;
        }
        return null;
    }

    public int updateById(WxMpMedia wxMpMedia)
    {
        return jdbcTemplate.update(UPDATE_BY_ID,
            new Object[] {wxMpMedia.getName(), wxMpMedia.getType(), wxMpMedia.getMediaId(), wxMpMedia.getCreateTime(),
                wxMpMedia.getExpireTime(), wxMpMedia.getId()});
    }

}
