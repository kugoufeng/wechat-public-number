package cn.jeremy.wechat.stock.bean;

import lombok.Data;

@Data
public class CountStockTradeData extends BaseStockData {

    private int countOneDayTrade;

    private int countThreeDayTrade;

    private int countFiveDayTrade;

    private int countTenDayTrade;

    private int countTwentyDayTrade;

    private int countOtherDayTrade;

    public CountStockTradeData() {
    }

    public CountStockTradeData(String name, String num) {
        super(name, num);
    }

    @Override
    public String toString() {
        return "CountStockTradeData{" +
                "countOneDayTrade=" + countOneDayTrade +
                ", countThreeDayTrade=" + countThreeDayTrade +
                ", countFiveDayTrade=" + countFiveDayTrade +
                ", countTenDayTrade=" + countTenDayTrade +
                ", countTwentyDayTrade=" + countTwentyDayTrade +
                ", countOtherDayTrade=" + countOtherDayTrade +
                "} " + super.toString();
    }
}
