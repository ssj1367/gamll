package com.atguigu.gmall.manager.test;

import com.atguigu.gmall.util.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test2 {
    @Autowired
    RedisUtil redisUtil;

    @Test
    public void test1(){
        Jedis jedis = redisUtil.getJedis();
        String ping = jedis.ping();
        System.out.print(ping);
    }
}
