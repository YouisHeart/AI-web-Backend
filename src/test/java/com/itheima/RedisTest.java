package com.itheima;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedisTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Test
    public void testSet() {
        // 往redis里面存一个键值对，StringRedisTemplate
        ValueOperations<String,String> operations = stringRedisTemplate.opsForValue();

        operations.set("username","gcj");
        // redis设置过期时间
        operations.set("id","1",15, TimeUnit.SECONDS);
    }

    // 从redis中获取username的值
    public void testGet() {
        // 从redis中获取一个键值对
        ValueOperations<String,String> operations = stringRedisTemplate.opsForValue();
        System.out.println(operations.get("username"));
    }
}
