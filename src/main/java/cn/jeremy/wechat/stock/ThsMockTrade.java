package cn.jeremy.wechat.stock;

import cn.jeremy.common.utils.DateTools;
import cn.jeremy.common.utils.FileUtil;
import cn.jeremy.common.utils.HttpTools;
import cn.jeremy.common.utils.StringTools;
import cn.jeremy.common.utils.bean.HttpResult;
import cn.jeremy.wechat.entity.BaseStockData;
import cn.jeremy.wechat.stock.bean.StockCloseData;
import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 同花顺网站交易实现
 *
 * @author kugoufeng
 * @date 2017/12/21 下午 6:44
 */
@Service
public class ThsMockTrade implements Trade
{
    private static final Logger LOGGER = LogManager.getLogger(ThsMockTrade.class);

    /**
     * 请求头信息
     */
    private static final String MOCK_REQ_HEADER = ThsAutoLogin.getThsTradeCookie();

    /**
     * 分页查询a股市场所有股票代码url
     */
    @Value("${ths.a.mark.stocks.url}")
    private String pageAMarkStocksUrl;

    /**
     * 查询个股当天收盘数据url
     */
    @Value("${ths.stock.close.data.url}")
    private String stockCloseDataUrl;

    /**
     * 查询个股当天资金成交数据url
     */
    @Value("${ths.stock.trade.data.url}")
    private String stockTradeDataUrl;

    @Value("${file.download.basepath}")
    private String basePath;


    private static final String CREATE_TABLE_SQL = "CREATE TABLE IF NOT EXISTS `stock_%s` (\n" +
        "  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',\n" +
        "  `open_price` int(11) NOT NULL COMMENT '开盘价格，单位分',\n" +
        "  `top_price` int(11) NOT NULL COMMENT '当天股票最高价格，单位分',\n" +
        "  `low_price` int(11) NOT NULL COMMENT '当天股票最低价格，单位分',\n" +
        "  `yest_close_price` int(11) NOT NULL COMMENT '昨天收盘价格，单位分',\n" +
        "  `close_price` int(11) NOT NULL COMMENT '当天股票收盘价格，单位分',\n" +
        "  `chg` int(5) NOT NULL COMMENT '股票涨跌百分比分子，分母10000',\n" +
        "  `zlc` int(11) NOT NULL COMMENT '当天的资金总流出，单位百元',\n" +
        "  `zlr` int(11) NOT NULL COMMENT '当天的资金总流如，单位百元',\n" +
        "  `je` int(11) NOT NULL COMMENT '当天资金净额，单位百元',\n" +
        "  `today` date NOT NULL COMMENT '当天的日期',\n" +
        "  PRIMARY KEY (`id`),\n" +
        "  UNIQUE KEY `uni_today` (`today`) USING HASH\n" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='%s, 收盘数据';";

    private static final ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
        .setNameFormat("thsMockTrade-pool-%d").build();

    private static final ExecutorService singleThreadPool = new ThreadPoolExecutor(5, 10,
        0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<Runnable>(1024), namedThreadFactory, new ThreadPoolExecutor.AbortPolicy());

    private static final Lock lock = new ReentrantLock();

    @Autowired
    JdbcTemplate jdbcTemplate;


    @Override
    public String getRequestCookie()
    {
        return MOCK_REQ_HEADER;
    }

    @Override
    @Scheduled(cron = "0 0 23 ? * MON-FRI")
    public void updateAMarkStocks()
    {
        singleThreadPool.execute(() -> {
            //查询第一页数据
            try
            {
                if (lock.tryLock(10, TimeUnit.MILLISECONDS))
                {
                    try
                    {
                        String responseBody = pageStock(1);
                        int totalPage = getTotalPageFromXML(responseBody);
                        List<BaseStockData> stockListFromXML = getStockListFromXML(responseBody);
                        List<BaseStockData> list = new ArrayList<>();
                        if (!CollectionUtils.isEmpty(stockListFromXML))
                        {
                            list.addAll(stockListFromXML);
                        }
                        if (totalPage > 1)
                        {
                            for (int i = 2; i < totalPage + 1; i++)
                            {
                                responseBody = pageStock(i);
                                stockListFromXML = getStockListFromXML(responseBody);
                                if (!CollectionUtils.isEmpty(stockListFromXML))
                                {
                                    list.addAll(stockListFromXML);
                                }
                            }
                        }
                        for (BaseStockData baseStockData : list)
                        {
                            Integer count = queryForObject("select count(1) from stock_base where num = ?",
                                new Object[] {baseStockData.getNum()},
                                Integer.class);
                            if (count == null || count == 0)
                            {
                                jdbcTemplate.update("insert into stock_base values (?, ?)",
                                    new Object[] {baseStockData.getName(), baseStockData.getNum()});
                            }
                            else
                            {
                                jdbcTemplate.update("update stock_base set name = ? where num = ?",
                                    new Object[] {baseStockData.getName(), baseStockData.getNum()});
                            }
                            //创建新的表格
                            jdbcTemplate.update(String.format(CREATE_TABLE_SQL,
                                baseStockData.getNum(),
                                baseStockData.getName()));
                        }
                        LOGGER.info("updateAMarkStocks over");
                    }
                    catch (Exception e)
                    {
                        LOGGER.error("updateAMarkStocks has error, e:{}", e);
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
            }
            catch (InterruptedException e)
            {
                //不做操作
            }
        });
    }

    /**
     * 分页查询股票列表
     *
     * @param page
     * @return java.lang.String
     * @author fengjiangtao
     */
    private String pageStock(int page)
    {
        String url = pageAMarkStocksUrl.replace("{page}", page + "");
        HttpResult result =
            HttpTools.getInstance()
                .sendHttpRequestByGet(url, MOCK_REQ_HEADER, "gbk");
        if (result.getRespCode() != HttpStatus.SC_OK)
        {
            return null;
        }
        return result.getResponseBody();
    }

    /**
     * 从xml数据中，分离出总的分页数据
     *
     * @param respXML
     * @return int
     * @author fengjiangtao
     */
    private int getTotalPageFromXML(String respXML)
    {
        if (StringUtils.isEmpty(respXML))
        {
            LOGGER.error("respXML is empty");
            return 0;
        }
        Document document = Jsoup.parse(respXML);
        Elements span = document.select(".page_info");
        // currentPage/totalPage
        String text = span.get(0).text();
        String[] split = text.split("/");
        return Integer.parseInt(split[1]);
    }

    /**
     * 从xml数据中分离出股票名称、代码列表
     *
     * @param respXML
     * @return java.util.List<cn.jeremy.stock.bean.BaseStockData>
     * @author fengjiangtao
     */
    private List<BaseStockData> getStockListFromXML(String respXML)
    {
        if (StringUtils.isEmpty(respXML))
        {
            LOGGER.error("respXML is empty");
            return null;
        }
        Document document = Jsoup.parse(respXML);
        Elements tbodyElements = document.select("tbody");
        if (tbodyElements == null)
        {
            return null;
        }
        Elements trElements = tbodyElements.get(0).select("tr");
        if (trElements == null)
        {
            return null;
        }
        List<BaseStockData> list = new ArrayList<>(trElements.size());
        for (Element element : trElements)
        {
            Elements tdElements = element.select("td");
            String stockNum = tdElements.get(1).select("a").get(0).text();
            String stockName = tdElements.get(2).select("a").get(0).text();
            list.add(new BaseStockData(stockName, stockNum));
        }
        return list;
    }

    @Override
    @Scheduled(cron = "0 31 11,15 ? * MON-FRI")
    public void updateStockCloseData()
    {
        singleThreadPool.execute(() -> {
            try
            {
                if (lock.tryLock(10, TimeUnit.MILLISECONDS))
                {
                    try
                    {
                        List<String> numList =
                            jdbcTemplate.queryForList("select num from stock_base", new Object[] {}, String.class);
                        int i = 1;
                        for (String num : numList)
                        {
                            updateStockCloseData(num);
                            LOGGER.info("updateStockCloseData progress, {}/{}", i++, numList.size());
                            Thread.sleep(200);
                        }
                        genStockDayReport();
                        LOGGER.info("updateStockCloseData over");
                    }
                    catch (Exception e)
                    {
                        LOGGER.error("updateStockCloseData has error, e:{}", e);
                    }
                    finally
                    {
                        lock.unlock();
                    }
                }
            }
            catch (InterruptedException e)
            {
                //不做任何操作
            }
        });

    }

    @Override
    public void updateStockCloseData(String num)
    {
        StockCloseData stockCloseData = getStockCloseData(num);
        if (null != stockCloseData)
        {
            setStockTradeData(stockCloseData, num);
            insertOrUpdateStockCloseData(stockCloseData, num);
        }
    }

    public StockCloseData getStockCloseData(String num)
    {
        String stockCloseDataUrl = this.stockCloseDataUrl.replace("{num}", num);
        HttpResult result =
            HttpTools.getInstance()
                .sendHttpRequestByGet(stockCloseDataUrl,
                    "Referer:http://stockpage.10jqka.com.cn/realHead_v2.html",
                    "gbk");
        if (result.getRespCode() != HttpStatus.SC_OK)
        {
            return null;
        }
        String responseBody = result.getResponseBody();
        //去掉jsonp包装
        String respJson = responseBody.substring(responseBody.indexOf("(") + 1, responseBody.length() - 1);
        JSONObject jsonObject = JSONObject.parseObject(respJson).getJSONObject("items");
        String updateTimeStr = jsonObject.getString("updateTime").substring(0, 10);

        Date updateTime = DateTools.timeStr2Date(updateTimeStr, DateTools.DATE_FORMAT_10);
        StockCloseData stockCloseData = new StockCloseData(updateTime);
        stockCloseData.setOpenPrice(StringTools.yuanToFen(jsonObject.getString("7")));
        stockCloseData.setTopPrice(StringTools.yuanToFen(jsonObject.getString("8")));
        stockCloseData.setLowPrice(StringTools.yuanToFen(jsonObject.getString("9")));
        stockCloseData.setYestClosePrice(StringTools.yuanToFen(jsonObject.getString("6")));
        stockCloseData.setClosePrice(StringTools.yuanToFen(jsonObject.getString("10")));
        stockCloseData.setChg(StringTools.yuanToFen(jsonObject.getString("199112")));
        return stockCloseData;
    }

    private void setStockTradeData(StockCloseData stockCloseData, String num)
    {
        String stockTradeDataUrl = this.stockTradeDataUrl.replace("{num}", num);
        HttpResult result =
            HttpTools.getInstance()
                .sendHttpRequestByGet(stockTradeDataUrl, null, "gbk");
        if (result.getRespCode() != HttpStatus.SC_OK)
        {
            return;
        }
        String responseBody = result.getResponseBody();
        JSONObject jsonObject = JSONObject.parseObject(responseBody);
        JSONObject title = jsonObject.getJSONObject("title");
        stockCloseData.setZlc(StringTools.yuanToFen(title.getString("zlc")));
        stockCloseData.setZlr(StringTools.yuanToFen(title.getString("zlr")));
        stockCloseData.setJe(StringTools.yuanToFen(title.getString("je")));
    }

    private void insertOrUpdateStockCloseData(StockCloseData stockCloseData, String num)
    {
        String selectSql = String.format("select id from stock_%s where Date(today) = ?", num);
        try
        {
            Integer id = queryForObject(selectSql,
                new Object[] {stockCloseData.getToday()},
                Integer.class);
            if (id != null)
            {
                String updateSql = String.format(
                    "update stock_%s set open_price = ?, top_price = ?, low_price = ?, yest_close_price = ?, " +
                        "close_price = ?, chg = ?, zlc = ?, zlr = ?, je = ? where id = ?", num);
                jdbcTemplate.update(updateSql,
                    new Object[] {stockCloseData.getOpenPrice(), stockCloseData.getTopPrice(),
                        stockCloseData.getLowPrice(), stockCloseData.getYestClosePrice(),
                        stockCloseData.getClosePrice(), stockCloseData.getChg(), stockCloseData.getZlc(),
                        stockCloseData.getZlr(), stockCloseData.getJe(), id});
            }
            else
            {
                String insertSql = String.format(
                    "insert into stock_%s (open_price, top_price, low_price, yest_close_price, close_price, chg, zlc," +
                        " zlr, je, today) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", num);
                jdbcTemplate.update(insertSql,
                    new Object[] {stockCloseData.getOpenPrice(), stockCloseData.getTopPrice(),
                        stockCloseData.getLowPrice(), stockCloseData.getYestClosePrice(),
                        stockCloseData.getClosePrice(), stockCloseData.getChg(), stockCloseData.getZlc(),
                        stockCloseData.getZlr(), stockCloseData.getJe(), stockCloseData.getToday()});
            }
        }
        catch (Exception e)
        {
            LOGGER.error("insertOrUpdateStockCloseData has error, e:{}", e);
        }

    }

    private <T> T queryForObject(String sql, Object[] args, Class<T> requiredType)
        throws DataAccessException
    {
        try
        {
            return jdbcTemplate.queryForObject(sql, args, requiredType);
        }
        catch (EmptyResultDataAccessException e)
        {
            return null;
        }
    }

    /**
     * 生成股票日报txt文件
     *
     * @author fengjiangtao
     */
    public void genStockDayReport()
    {
        List<BaseStockData> baseStockDataList = new ArrayList<>();
        jdbcTemplate.query("select * from stock_base", new Object[] {}, (resultSet) -> {
            BaseStockData baseStockData = new BaseStockData();
            baseStockData.setName(resultSet.getString(1));
            baseStockData.setNum(resultSet.getString(2));
            baseStockDataList.add(baseStockData);
        });
        FileUtil.deleteDir(basePath.concat("raw"));
        for (BaseStockData baseStockData : baseStockDataList)
        {
            genStockDayReport(baseStockData.getNum(), baseStockData.getName());
        }

    }

    public void genStockDayReport(String num, String name)
    {
        String selectSql = String.format("select * from stock_%s ORDER BY today DESC", num);
        List<StockCloseData> baseStockDataList = new ArrayList<>();
        jdbcTemplate.query(selectSql, new Object[] {}, (resultSet) ->
        {
            StockCloseData stockCloseData = new StockCloseData(resultSet.getDate(11));
            stockCloseData.setNum(num);
            stockCloseData.setName(name);
            stockCloseData.setOpenPrice(resultSet.getInt(2));
            stockCloseData.setTopPrice(resultSet.getInt(3));
            stockCloseData.setLowPrice(resultSet.getInt(4));
            stockCloseData.setYestClosePrice(resultSet.getInt(5));
            stockCloseData.setClosePrice(resultSet.getInt(6));
            stockCloseData.setChg(resultSet.getInt(7));
            stockCloseData.setZlc(resultSet.getInt(8));
            stockCloseData.setZlr(resultSet.getInt(9));
            stockCloseData.setJe(resultSet.getInt(10));
            baseStockDataList.add(stockCloseData);
        });
        baseStockDataList.forEach(s -> {
            s.setName(name);
            s.setNum(num);
            String filePath = basePath.concat("raw/").concat("raw_stock.txt");
            FileUtil.writeFileFromString(filePath, s.toString(), true);
        });
    }

}
