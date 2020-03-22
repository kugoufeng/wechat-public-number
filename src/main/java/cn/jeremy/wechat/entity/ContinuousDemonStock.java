package cn.jeremy.wechat.entity;

import lombok.Data;

import java.util.Date;

@Data
public class ContinuousDemonStock extends DemonStock
{

    private int countDay;

    public ContinuousDemonStock() {
    }

    public ContinuousDemonStock(int countDay) {
        this.countDay = countDay;
    }

    public ContinuousDemonStock(Date selectDate, int countDay) {
        super(selectDate);
        this.countDay = countDay;
    }

    public ContinuousDemonStock(String name, String num, Date selectDate, int countDay) {
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
