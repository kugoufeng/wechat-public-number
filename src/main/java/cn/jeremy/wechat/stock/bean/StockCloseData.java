package cn.jeremy.wechat.stock.bean;

import cn.jeremy.common.utils.DateTools;
import cn.jeremy.wechat.entity.BaseStockData;
import lombok.Data;

import java.util.Date;

/**
 * 股票收盘数据
 *
 * @author fengjiangtao
 * @date 2020/2/27 21:29
 */
@Data
public class StockCloseData extends BaseStockData
{

    /**
     * 股票开盘价格
     */
    private int openPrice;

    /**
     * 股票最高价格
     */
    private int topPrice;

    /**
     * 股票最低价格
     */
    private int lowPrice;

    /**
     * 昨天收盘价格
     */
    private int yestClosePrice;

    /**
     * 股票收盘价格
     */
    private int closePrice;

    /**
     * 股票涨跌
     */
    private int chg;

    /**
     * 资金总流出（单位百元）
     */
    private int zlc;

    /**
     * 资金总流入（单位百元）
     */
    private int zlr;

    /**
     * 资金净额（单位百元）
     */
    private int je;

    /**
     * 当天日期
     */
    private Date today;

    public StockCloseData(Date today) {
        this.today = today;
    }

    public StockCloseData(String name, String num, Date today) {
        super(name, num);
        this.today = today;
    }

    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        sb.append(getName()).append("\t");
        sb.append(getNum()).append("\t");
        sb.append(openPrice).append("\t");
        sb.append(topPrice).append("\t");
        sb.append(lowPrice).append("\t");
        sb.append(yestClosePrice).append("\t");
        sb.append(closePrice).append("\t");
        sb.append(chg).append("\t");
        sb.append(zlc).append("\t");
        sb.append(zlr).append("\t");
        sb.append(je).append("\t");
        sb.append(DateTools.date2TimeStr(today, DateTools.DATE_FORMAT_10)).append("\n");
        return sb.toString();
    }

}
