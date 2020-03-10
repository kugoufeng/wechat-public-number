package cn.jeremy.stock.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController
{
    @Autowired
    JdbcTemplate jdbcTemplate;

    @RequestMapping(value="/hello", method=RequestMethod.GET)
    public String index() {

        String sql = "SELECT count(1) FROM stock_000001";

        // 通过jdbcTemplate查询数据库
        Integer mobile = jdbcTemplate.queryForObject(
            sql, new Object[] {}, Integer.class);

        return "Hello " + mobile;
    }
}
