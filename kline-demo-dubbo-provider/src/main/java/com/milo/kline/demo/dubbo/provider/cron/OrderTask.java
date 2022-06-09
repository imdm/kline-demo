package com.milo.kline.demo.dubbo.provider.cron;

import com.milo.kline.demo.dubbo.provider.entity.Order;
import com.milo.kline.demo.dubbo.provider.mapper.OrderMapper;
import com.milo.kline.demo.dubbo.provider.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OrderTask {
    private final OrderService oSvc;

    @Autowired
    public OrderTask(OrderService oSvc) {
       this.oSvc = oSvc;
    }

    @Scheduled(fixedDelay = 500)
    public void autoGenOrder() {
        Order o = new Order();
        Random r = new Random();
        o.setPrice(r.nextDouble(100));
        o.setQuantity(r.nextDouble(1000));
        o.setTimestamp(System.currentTimeMillis());
        oSvc.saveOrder(o);
        try {
            Thread.sleep(r.nextLong(3000));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
