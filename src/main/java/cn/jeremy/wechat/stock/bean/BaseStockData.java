package cn.jeremy.wechat.stock.bean;

import lombok.Data;

@Data
public class BaseStockData
{
    /**
     * 股票名称
     */
    private String name;

    /**
     * 股票代号
     */
    private String num;

    public BaseStockData()
    {
    }

    public BaseStockData(String name, String num)
    {
        this.name = name;
        this.num = num;
    }

    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer("BaseStockData{");
        sb.append("name='").append(name).append('\'');
        sb.append(", num='").append(num).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
