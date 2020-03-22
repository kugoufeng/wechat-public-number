package cn.jeremy.wechat.service;

import cn.jeremy.wechat.entity.ApiAuthority;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class ApiAuthorityService
{
    @Autowired
    JdbcTemplate jdbcTemplate;

    private static final Map<Integer, Integer> APIMAP = new HashMap<>();

    private static final String INSERT =
        "insert into api_authority (wx_mp_user_id, api_type, left_num) values (?, ?, ?)";

    private static final String SELECT_BY_USERID_AND_APITYPE =
        "select * from api_authority where wx_mp_user_id = ? and api_type = ?";

    private static final String ADD_LEFT_NUM = "update api_authority set left_num = left_num + ? where id = ?";

    private static final String DECR_LEFT_NUM =
        "update api_authority set left_num = left_num - 1 where id = ? and left_num > 0";

    @PostConstruct
    public void init()
    {
        APIMAP.put(1, 30);
    }

    public int insert(Integer userId, Integer apiType)
    {
        Integer leftNum = null;
        if ((leftNum = APIMAP.get(apiType)) == null)
        {
            return -1;
        }
        return jdbcTemplate.update(INSERT, new Object[] {userId, apiType, leftNum});
    }

    public ApiAuthority selectByUserIdAndAPIType(Integer userId, Integer apiType)
    {
        ApiAuthority apiAuthority = new ApiAuthority();
        jdbcTemplate.query(SELECT_BY_USERID_AND_APITYPE, new Object[] {userId, apiType}, resultSet -> {
            apiAuthority.setId(resultSet.getInt(1));
            apiAuthority.setUserId(resultSet.getInt(2));
            apiAuthority.setApiType(resultSet.getInt(3));
            apiAuthority.setLeftNum(resultSet.getInt(4));
        });
        if (apiAuthority.getId() != null)
        {
            return apiAuthority;
        }
        return null;
    }

    public int addLeftNum(Integer userId, Integer apiType)
    {
        ApiAuthority apiAuthority = selectByUserIdAndAPIType(userId, apiType);
        if (null == apiAuthority)
        {
            return insert(userId, apiType);
        }

        return jdbcTemplate.update(ADD_LEFT_NUM, new Object[] {APIMAP.get(apiType), apiAuthority.getId()});
    }

    public int decrLeftNum(Integer userId, Integer apiType)
    {
        ApiAuthority apiAuthority = selectByUserIdAndAPIType(userId, apiType);
        if (null == apiAuthority || apiAuthority.getLeftNum() < 1)
        {
            return -1;
        }

        return jdbcTemplate.update(DECR_LEFT_NUM, new Object[] {apiAuthority.getId()});
    }

}
