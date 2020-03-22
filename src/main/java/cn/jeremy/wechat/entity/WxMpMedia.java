package cn.jeremy.wechat.entity;

import java.util.Date;
import lombok.Data;

@Data
public class WxMpMedia
{
    private Integer id;

    private String name;

    private String type;

    private String mediaId;

    private Date createTime;

    private Date expireTime;

    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer("WxMpMedia{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", mediaId='").append(mediaId).append('\'');
        sb.append(", createTime=").append(createTime);
        sb.append(", expireTime=").append(expireTime);
        sb.append('}');
        return sb.toString();
    }
}
