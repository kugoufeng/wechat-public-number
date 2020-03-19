package cn.jeremy.wechat.stock.bean;

import lombok.Data;

import java.util.Date;

@Data
public class ContinuousSelectStockData extends SelectStockData {

    private int countDay;

    public ContinuousSelectStockData() {
    }

    public ContinuousSelectStockData(int countDay) {
        this.countDay = countDay;
    }

    public ContinuousSelectStockData(Date selectDate, int countDay) {
        super(selectDate);
        this.countDay = countDay;
    }

    public ContinuousSelectStockData(String name, String num, Date selectDate, int countDay) {
        super(name, num, selectDate);
        this.countDay = countDay;
    }

    @Override
    public String toString() {
        return "ContinuousSelectStockData{" +
                "countDay=" + countDay +
                "} " + super.toString();
    }
}
