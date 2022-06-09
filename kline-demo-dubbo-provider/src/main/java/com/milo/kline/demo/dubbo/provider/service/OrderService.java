package com.milo.kline.demo.dubbo.provider.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.milo.kline.demo.dubbo.provider.entity.Order;
import com.milo.kline.demo.dubbo.provider.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class OrderService {
    @Resource(type = OrderMapper.class)
    private OrderMapper om;
    @Autowired
    RedisTemplate<String, Object> rds;
    public List<Order> ordersByTimeRange(Long fromTimestamp, Long toTimestamp) {
        long cacheEnd = System.currentTimeMillis();
        if (toTimestamp > cacheEnd) {
            toTimestamp = cacheEnd;
        }
        long cacheBegin = cacheEnd - 70000; // 缓存70秒内订单
        long dbBegin=0, dbEnd =0;
        boolean hitCache = false;
        // 是否命中缓存
        if (cacheBegin<toTimestamp) {
            hitCache = true;
            if (cacheBegin>fromTimestamp) {
                dbBegin = fromTimestamp;
                dbEnd = cacheBegin;
            } else {
                cacheBegin = fromTimestamp;
            }
            if (cacheEnd>toTimestamp) {
                cacheEnd = toTimestamp;
            }
        }
        List<Order> ret = new ArrayList<>();
        if (hitCache) {
            ret.addAll(ordersFromRedis(cacheBegin, cacheEnd));
        }
        if(dbBegin>0) {
            ret.addAll(ordersFromDB(dbBegin, dbEnd));
        }
        return ret;
    }

    public boolean saveOrder(Order o) {
        boolean success = om.insert(o)>0;
        if (success) {
            rds.opsForZSet().addIfAbsent("orders", o, o.getTimestamp());
        }
        return success;
    }

    private List<Order> ordersFromRedis(Long from, Long to) {
        Set<Object> orders = rds.opsForZSet().rangeByScore("orders", from, to);
        List<Order> ret = new ArrayList<>();
        if (orders != null) {
            for (Object o: orders) {
                ret.add((Order)o);
            }
        }
        return ret;
    }

    private List<Order> ordersFromDB(Long fromTimestamp, Long toTimestamp){
        return om.selectList(Wrappers.<Order>query().ge("timestamp", fromTimestamp).le("timestamp", toTimestamp).orderByAsc("timestamp"));
    }
}
