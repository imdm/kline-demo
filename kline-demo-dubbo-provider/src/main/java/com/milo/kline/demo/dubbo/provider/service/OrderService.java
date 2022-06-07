package com.milo.kline.demo.dubbo.provider.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.milo.kline.demo.dubbo.provider.entity.Order;
import com.milo.kline.demo.dubbo.provider.mapper.OrderMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class OrderService {
    @Resource(type = OrderMapper.class)
    private OrderMapper om;
    @Cacheable(value = "ordersByTimeRange")
    public List<Order> ordersByTimeRange(Long fromTimestamp, Long toTimestamp) {
        return om.selectList(Wrappers.<Order>query().ge("timestamp", fromTimestamp).le("timestamp", toTimestamp).orderByAsc("timestamp"));
    }
}
