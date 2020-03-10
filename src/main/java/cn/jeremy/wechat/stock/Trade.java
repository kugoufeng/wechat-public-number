package cn.jeremy.wechat.stock;


/**
 * 股票的交易接口
 *
 * @author kugoufeng
 * @date 2017/12/22 下午 6:20
 */
public interface Trade
{

    /**
     * 获取访问的cookie
     *
     * @author fengjiangtao
     */
    String getRequestCookie();


    /**
     * 同步a股市场所有股票代码
     *
     *
     * @return
     * @throws
     * @author fengjiangtao
     */
    void updateAMarkStocks();

    /**
     * 更新全部个股收盘时数据
     *
     *
     * @return
     * @throws
     * @author fengjiangtao
     */
    void updateStockCloseData();

    /**
     * 更新个股收盘时数据
     *
     *
     * @return
     * @throws
     * @author fengjiangtao
     */
    void updateStockCloseData(String num);
}
