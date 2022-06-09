package com.milo.kline.demo.dubbo.provider;

import com.milo.kline.demo.dubbo.provider.cron.KlineManager;
import com.milo.kline.demo.dubbo.provider.mapper.KlineMapper;
import com.milo.kline.demo.dubbo.provider.service.KlineService;
import com.milo.kline.demo.dubbo.provider.service.OrderService;
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableDubbo
@MapperScan("com.milo.kline.demo.dubbo.provider.mapper")
@EnableScheduling
@EnableCaching
@SpringBootApplication
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class, args);
    }
}
