package cn.jeremy.wechat.entity;

import lombok.Data;

@Data
public class ApiAuthority
{
    private Integer id;

    private Integer userId;

    private Integer apiType;

    private Integer leftNum;
}
