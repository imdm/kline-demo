package com.milo.kline.demo.dubbo.provider.cron;

import com.milo.kline.demo.dubbo.provider.entity.Order;
import com.milo.kline.demo.dubbo.provider.mapper.KlineMapper;
import com.milo.kline.demo.dubbo.provider.entity.Kline;
import com.milo.kline.demo.dubbo.provider.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
public class KlineTask {
    @Resource(type = OrderService.class)
    OrderService oSvc;
    @Resource(type = KlineMapper.class)
    KlineMapper km;

    private LocalDateTime beginOfMinute() {
        LocalDateTime now = LocalDateTime.now();
        return LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), now.getMinute());
    }

    private Long toTimestamp(LocalDateTime t) {
        return t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    private void doGenChart(LocalDateTime from, LocalDateTime to, String period) {
        Long fromTs = toTimestamp(from);
        Long toTs = toTimestamp(to);
        List<Order> orders = this.oSvc.ordersByTimeRange(fromTs, toTs);
        Kline c = new Kline(null,period,fromTs,toTs,0D,0D,0D,Double.MAX_VALUE,0D);
        int oSize = orders.size();
        log.info("本次查询到{}条订单", oSize);
        for (int i = 0; i < oSize; i++) {
            Order o = orders.get(i);
            Double price = o.getPrice();
            if (i == 0) {
                c.setOpen(price);
            }
            if (i == oSize - 1) {
                c.setClose(price);
            }
            if (c.getHighest()<price) {
                c.setHighest(price);
            }
            if (c.getLowest()>price) {
                c.setLowest(price);
            }
            c.setVolume(c.getVolume()+o.getQuantity());
        }
        km.insert(c);
    }

    @Scheduled(fixedRate = 60000, initialDelay = 60000)
    public void gen1m() {
        log.info("auto gen 1m chart...");
        LocalDateTime to = beginOfMinute();
        LocalDateTime from = to.minusMinutes(1);
        doGenChart(from, to, "1m");
    }

    @Scheduled(fixedRate = 300000, initialDelay = 300000)
    public void gen5m() {
        log.info("auto gen 5m chart...");
        LocalDateTime to = beginOfMinute();
        LocalDateTime from = to.minusMinutes(5);
        doGenChart(from, to, "5m");
    }
    @Scheduled(fixedRate = 900000, initialDelay = 900000)
    public void gen15m() {
        log.info("auto gen 15m chart...");
        LocalDateTime to = beginOfMinute();
        LocalDateTime from = to.minusMinutes(15);
        doGenChart(from, to, "15m");
    }
    @Scheduled(fixedRate = 1800000, initialDelay = 1800000)
    public void gen30m() {
        log.info("auto gen 30m chart...");
        LocalDateTime to = beginOfMinute();
        LocalDateTime from = to.minusMinutes(30);
        doGenChart(from, to, "30m");
    }
}
