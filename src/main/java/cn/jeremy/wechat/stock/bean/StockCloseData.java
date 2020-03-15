package cn.jeremy.wechat.stock.bean;

import cn.jeremy.common.utils.DateTools;
import java.util.Date;

/**
 * 股票收盘数据
 *
 * @author fengjiangtao
 * @date 2020/2/27 21:29
 */
public class StockCloseData
{
    /**
     * 股票名称
     */
    private String name;

    /**
     * 股票代号
     */
    private String num;

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

    public StockCloseData(Date today)
    {
        this.today = today;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getNum()
    {
        return num;
    }

    public void setNum(String num)
    {
        this.num = num;
    }

    public int getOpenPrice()
    {
        return openPrice;
    }

    public void setOpenPrice(int openPrice)
    {
        this.openPrice = openPrice;
    }

    public int getTopPrice()
    {
        return topPrice;
    }

    public void setTopPrice(int topPrice)
    {
        this.topPrice = topPrice;
    }

    public int getLowPrice()
    {
        return lowPrice;
    }

    public void setLowPrice(int lowPrice)
    {
        this.lowPrice = lowPrice;
    }

    public int getYestClosePrice()
    {
        return yestClosePrice;
    }

    public void setYestClosePrice(int yestClosePrice)
    {
        this.yestClosePrice = yestClosePrice;
    }

    public int getClosePrice()
    {
        return closePrice;
    }

    public void setClosePrice(int closePrice)
    {
        this.closePrice = closePrice;
    }

    public int getChg()
    {
        return chg;
    }

    public void setChg(int chg)
    {
        this.chg = chg;
    }

    public int getZlc()
    {
        return zlc;
    }

    public void setZlc(int zlc)
    {
        this.zlc = zlc;
    }

    public int getZlr()
    {
        return zlr;
    }

    public void setZlr(int zlr)
    {
        this.zlr = zlr;
    }

    public int getJe()
    {
        return je;
    }

    public void setJe(int je)
    {
        this.je = je;
    }

    public Date getToday()
    {
        return today;
    }

    public void setToday(Date today)
    {
        this.today = today;
    }

    @Override
    public String toString()
    {
        final StringBuffer sb = new StringBuffer();
        sb.append(name).append("\t");
        sb.append(num).append("\t");
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
