package cn.jeremy.wechat.bean;

import cn.jeremy.wechat.entity.BaseStockData;
import com.alibaba.fastjson.annotation.JSONField;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author fengjiangtao
 * @date 2020/3/29 0:25
 */
@Getter
@Setter
@ToString
public class DemonStockHistory extends BaseStockData
{
    @JSONField(format = "yyyy-MM-dd")
    private Date selectDate;

    public DemonStockHistory(String name, String num, Date selectDate)
    {
        super(name, num);
        this.selectDate = selectDate;
    }

    private DemonStockBuyElement demonStockBuyElement;

    private List<DemonStockSellElement> demonStockSellElements;

    @Getter
    @Setter
    @ToString
    public static class BaseDemonStockElement
    {
        int openPrice;

        int topPrice;

        int lowPrice;

        int closePrice;

        boolean isEarning;

        public BaseDemonStockElement(int openPrice, int topPrice, int lowPrice, int closePrice)
        {
            this.openPrice = openPrice;
            this.topPrice = topPrice;
            this.lowPrice = lowPrice;
            this.closePrice = closePrice;
        }

    }

    @Getter
    @Setter
    @ToString
    public static class DemonStockBuyElement extends BaseDemonStockElement
    {
        private int closeChg;

        private int buyOnePrice;

        private boolean isCanBuyOne;

        private int buyTwoPrice;

        private boolean isCanBuyTwo;

        private int buyThreePrice;

        private boolean isCanBuyThree;

        private int buyFourPrice;

        private boolean isCanBuyFour;

        private int totalPositions;

        private int avgPrice;

        public DemonStockBuyElement(int openPrice, int topPrice, int lowPrice, int closePrice, int closeChg)
        {
            super(openPrice, topPrice, lowPrice, closePrice);
            this.closeChg = closeChg;
            this.buyOnePrice = Double.valueOf(Math.floor(this.openPrice * (1 - 0.018))).intValue();
            this.isCanBuyOne = canBuy(this.buyOnePrice);
            this.buyTwoPrice = Double.valueOf(Math.floor(this.openPrice * (1 - 0.028))).intValue();
            this.isCanBuyTwo = canBuy(this.buyTwoPrice);
            this.buyThreePrice = Double.valueOf(Math.floor(this.openPrice * (1 - 0.038))).intValue();
            this.isCanBuyThree = canBuy(this.buyThreePrice);
            this.buyFourPrice = Double.valueOf(Math.floor(this.openPrice * (1 - 0.048))).intValue();
            this.isCanBuyFour = canBuy(this.buyFourPrice);
            this.totalPositions =
                (isCanBuyOne ? 1 : 0) + (isCanBuyTwo ? 2 : 0) + (isCanBuyThree ? 3 : 0) + (isCanBuyFour ? 4 : 0);
            //沒有达到买入价位，以收盘价买入一层底仓
            if (this.totalPositions == 0 && closeChg < 800)
            {
                this.buyOnePrice = this.closePrice;
                this.isCanBuyOne = true;
                this.totalPositions = 1;
            }
            this.avgPrice = this.totalPositions == 0
                ? this.closePrice
                : (this.buyOnePrice * (isCanBuyOne ? 1 : 0) + this.buyTwoPrice * (isCanBuyTwo ? 2 : 0) +
                    this.buyThreePrice * (isCanBuyThree ? 3 : 0) + this.buyFourPrice * (isCanBuyFour ? 4 : 0)) /
                    this.totalPositions;
            this.isEarning = this.closePrice > this.avgPrice;
        }

        boolean canBuy(int price)
        {
            return price >= this.lowPrice;
        }

    }

    @Getter
    @Setter
    @ToString
    public static class DemonStockSellElement extends BaseDemonStockElement
    {

        private int buyPrice;

        private int totalPositions;

        private int stopLossPrice;

        private boolean isCanStopLoss;

        private int stopEarningPrice;

        private boolean isCanStopEarning;

        public DemonStockSellElement(int openPrice, int topPrice, int lowPrice, int closePrice, int buyPrice,
            int totalPositions)
        {
            super(openPrice, topPrice, lowPrice, closePrice);
            this.buyPrice = buyPrice;
            this.totalPositions = totalPositions;
            this.stopLossPrice =
                this.totalPositions > 0 ? Double.valueOf(Math.floor(this.buyPrice * (1 - 0.07))).intValue() : 0;
            this.isCanStopLoss = canStopLoss();
            this.stopEarningPrice =
                this.totalPositions > 0 ? Double.valueOf(Math.floor(this.buyPrice * (1 + 0.07))).intValue() : 0;
            this.isCanStopEarning = canStopEarning();
            this.isEarning = this.closePrice > this.buyPrice;
        }

        boolean canStopLoss()
        {
            return this.stopLossPrice >= this.lowPrice && this.totalPositions > 0;
        }

        boolean canStopEarning()
        {
            return this.stopEarningPrice <= this.topPrice && this.totalPositions > 0;
        }

    }
}
