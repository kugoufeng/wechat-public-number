package cn.jeremy.wechat.entity;

import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

@Data
public class ContinuousStockFund extends BaseStockData {

    private Integer oneDayTrade;

    private Integer threeDayTrade;

    private Integer fiveDayTrade;

    private Integer tenDayTrade;

    private Integer twentyDayTrade;

    private Integer otherDayTrade;

    public ContinuousStockFund() {
    }

    public ContinuousStockFund(String name, String num) {
        super(name, num);
    }

    /**
     * 根据传入的字段填充值，字段类似于 1|100
     *
     * @param value
     */
    public void fillValue(String value) {
        if (StringUtils.isNotEmpty(value)) {
            String[] split = value.split("\\|");
            if (ArrayUtils.isNotEmpty(split) && split.length == 2) {
                int day = NumberUtils.toInt(split[0]);
                int count = NumberUtils.toInt(split[1]);
                if (day == 1) {
                    this.setOneDayTrade(count);
                } else if (day == 3) {
                    this.setThreeDayTrade(count);
                } else if (day == 5) {
                    this.setFiveDayTrade(count);
                } else if (day == 10) {
                    this.setTenDayTrade(count);
                } else if (day == 20) {
                    this. setTwentyDayTrade(count);
                } else if (day > 0) {
                    this.setOtherDayTrade(count);
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
