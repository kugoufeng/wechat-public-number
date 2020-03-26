package cn.jeremy.wechat.entity;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

@Data
public class DemonStock extends BaseStockData {

    /** 潜伏天数 */
    private int day;

    /** 潜伏期内总的涨跌幅 */
    private int chg;

    /** 潜伏期内总的资金净额 */
    private int je;

    /** 选中当天涨跌幅 */
    private int selectChg;

    /** 选中当天资金净额 */
    private int selectJe;


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
