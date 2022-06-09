package com.milo.kline.demo.dubbo.provider.cron;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class CleanRedis {
    @Autowired
    private RedisTemplate<String, Object> rds;

    @Scheduled(fixedRate = 60000, initialDelay = 60000)
    public void autoCleanKline() {
        log.info("定时清空过期k线缓存");
        long now = System.currentTimeMillis();
        rds.opsForZSet().removeRangeByScore("kline1m", 0, now-86400000); // 1分钟k线保留最近1天的
        rds.opsForZSet().removeRangeByScore("kline5m", 0, now-432000000); // 5分钟k线保留最近5天的
        rds.opsForZSet().removeRangeByScore("kline15m", 0, now-1296000000); // 15分钟k线保留最近15天的
        rds.opsForZSet().removeRangeByScore("kline30m", 0, now-2592000000L); // 1分钟k线保留最近30天的
    }

    @Scheduled(fixedRate = 5000, initialDelay = 5000)
    public void autoCleanOrder() {
        log.info("定时清空过期订单缓存");
        long now = System.currentTimeMillis();
        rds.opsForZSet().removeRangeByScore("orders", 0, now-70000); // 保留近70秒内的订单
    }

//    @Resource(type = OrderService.class)
//    OrderService oSvc;
//    @Resource(type = KlineMapper.class)
//    KlineMapper km;
//
//    private LocalDateTime beginOfMinute() {
//        LocalDateTime now = LocalDateTime.now();
//        return LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), now.getMinute());
//    }
//
//    private Long toTimestamp(LocalDateTime t) {
//        return t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
//    }

//    private void doGenChart(LocalDateTime from, LocalDateTime to, String period) {
//        Long fromTs = toTimestamp(from);
//        Long toTs = toTimestamp(to);
//        List<Order> orders = this.oSvc.ordersByTimeRange(fromTs, toTs);
//        Kline c = new Kline(null,period,fromTs,toTs,0D,0D,0D,Double.MAX_VALUE,0D);
//        int oSize = orders.size();
//        log.info("本次查询到{}条订单", oSize);
//        for (int i = 0; i < oSize; i++) {
//            Order o = orders.get(i);
//            Double price = o.getPrice();
//            if (i == 0) {
//                c.setOpen(price);
//            }
//            if (i == oSize - 1) {
//                c.setClose(price);
//            }
//            if (c.getHighest()<price) {
//                c.setHighest(price);
//            }
//            if (c.getLowest()>price) {
//                c.setLowest(price);
//            }
//            c.setVolume(c.getVolume()+o.getQuantity());
//        }
//        km.insert(c);
//    }
//
//    @Scheduled(fixedRate = 60000, initialDelay = 60000)
//    public void gen1m() {
//        log.info("auto gen 1m chart...");
//        LocalDateTime to = beginOfMinute();
//        LocalDateTime from = to.minusMinutes(1);
//        doGenChart(from, to, "1m");
//    }
//
//    @Scheduled(fixedRate = 300000, initialDelay = 300000)
//    public void gen5m() {
//        log.info("auto gen 5m chart...");
//        LocalDateTime to = beginOfMinute();
//        LocalDateTime from = to.minusMinutes(5);
//        doGenChart(from, to, "5m");
//    }
//    @Scheduled(fixedRate = 900000, initialDelay = 900000)
//    public void gen15m() {
//        log.info("auto gen 15m chart...");
//        LocalDateTime to = beginOfMinute();
//        LocalDateTime from = to.minusMinutes(15);
//        doGenChart(from, to, "15m");
//    }
//    @Scheduled(fixedRate = 1800000, initialDelay = 1800000)
//    public void gen30m() {
//        log.info("auto gen 30m chart...");
//        LocalDateTime to = beginOfMinute();
//        LocalDateTime from = to.minusMinutes(30);
//        doGenChart(from, to, "30m");
//    }
}
