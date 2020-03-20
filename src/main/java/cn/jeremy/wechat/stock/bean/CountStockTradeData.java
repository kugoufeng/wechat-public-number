package cn.jeremy.wechat.stock.bean;

import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

@Data
public class CountStockTradeData extends BaseStockData {

    private Integer oneDayTrade;

    private Integer threeDayTrade;

    private Integer fiveDayTrade;

    private Integer tenDayTrade;

    private Integer twentyDayTrade;

    private Integer otherDayTrade;

    public CountStockTradeData() {
    }

    public CountStockTradeData(String name, String num) {
        super(name, num);
    }

    /**
     * 根据传入的字段填充值，字段类似于 1|100
     *
     * @param value
     */
    public void fillValue(String value) {
        if (StringUtils.isNotEmpty(value)) {
            String[] split = value.split("|");
            if (ArrayUtils.isNotEmpty(split) && split.length == 2) {
                int day = NumberUtils.toInt(split[0]);
                int count = NumberUtils.toInt(split[1]);
                if (day == 1) {
                    setOneDayTrade(count);
                } else if (day == 3) {
                    setThreeDayTrade(count);
                } else if (day == 5) {
                    setFiveDayTrade(count);
                } else if (day == 10) {
                    setTenDayTrade(count);
                } else if (day == 20) {
                    setTwentyDayTrade(count);
                } else if (day > 0) {
                    setOtherDayTrade(count);
                }
            }
        }
    }

    @Override
    public String toString() {
        return "CountStockTradeData{" +
                "oneDayTrade=" + oneDayTrade +
                ", threeDayTrade=" + threeDayTrade +
                ", fiveDayTrade=" + fiveDayTrade +
                ", tenDayTrade=" + tenDayTrade +
                ", twentyDayTrade=" + twentyDayTrade +
                ", otherDayTrade=" + otherDayTrade +
                "} " + super.toString();
    }
}
