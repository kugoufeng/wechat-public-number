package cn.jeremy.wechat.stock.bean;

import lombok.Data;

import java.util.Date;

@Data
public class SelectStockData extends BaseStockData {

    private Date selectDate;

    public SelectStockData() {
    }

    public SelectStockData(Date selectDate) {
        this.selectDate = selectDate;
    }

    public SelectStockData(String name, String num, Date selectDate) {
        super(name, num);
        this.selectDate = selectDate;
    }

    @Override
    public String toString() {
        return "SelectStockData{" +
                "selectDate=" + selectDate +
                "} " + super.toString();
    }
}
