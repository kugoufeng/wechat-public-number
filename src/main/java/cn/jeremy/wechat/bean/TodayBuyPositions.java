package cn.jeremy.wechat.bean;

import cn.jeremy.wechat.entity.BaseStockData;
import com.alibaba.fastjson.annotation.JSONField;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class TodayBuyPositions extends BaseStockData
{
    @JSONField(format = "yyyy-MM-dd")
    private Date selectDate;

    private int openPrice;

    private int buyOnePrice;

    private int buyOnePositions;

    private int buyTwoPrice;

    private int buyTwoPositions;

    private int buyThreePrice;

    private int buyThreePositions;

    private int buyFourPrice;

    private int buyFourPositions;

    public TodayBuyPositions(String name, String num, Date selectDate, int openPrice)
    {
        super(name, num);
        this.selectDate = selectDate;
        this.openPrice = openPrice;
        this.buyOnePrice = Double.valueOf(Math.floor(this.openPrice * (1 - 0.018))).intValue();
        this.buyTwoPrice = Double.valueOf(Math.floor(this.openPrice * (1 - 0.028))).intValue();
        this.buyThreePrice = Double.valueOf(Math.floor(this.openPrice * (1 - 0.038))).intValue();
        this.buyFourPrice = Double.valueOf(Math.floor(this.openPrice * (1 - 0.048))).intValue();
        //初始金额10w
        int money = 100000 * 100;
        this.buyOnePositions = (((money / 10) / this.buyOnePrice) / 100) * 100;
        this.buyTwoPositions = (((money * 2 / 10) / this.buyTwoPrice) / 100) * 100;
        this.buyThreePositions = (((money * 3 / 10) / this.buyThreePrice) / 100) * 100;
        this.buyFourPositions = (((money * 4 / 10) / this.buyFourPrice) / 100) * 100;
    }
}
