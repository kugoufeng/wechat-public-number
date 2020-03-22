package cn.jeremy.wechat.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

@Data
public class DemonStock extends BaseStockData {

    @JSONField(format = "yyyy-MM-dd")
    private Date selectDate;

    public DemonStock() {
    }

    public DemonStock(Date selectDate) {
        this.selectDate = selectDate;
    }

    public DemonStock(String name, String num, Date selectDate) {
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
