package com.milo.kline.demo.dubbo.provider.cron;

import com.milo.kline.demo.dubbo.provider.entity.Kline;
import com.milo.kline.demo.dubbo.provider.entity.Order;
import com.milo.kline.demo.dubbo.provider.service.KlineService;
import com.milo.kline.demo.dubbo.provider.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.*;

@Slf4j
@Component
public class KlineManager {
    private static volatile int signal = 1;
    private final static Object lock = new Object();
    private final RedisTemplate<String, Object> rds;
    private final ExecutorService pool;
    private final OrderService oSvc;
    private final KlineService kSvc;
    private Long ts1m; // 1分钟k线当前执行位置
    private Long ts5m; // 5分钟k线当前执行位置
    private Long ts15m; // 15分钟k线当前执行位置
    private Long ts30m; // 30分钟k线当前执行位置


    @Autowired
    public KlineManager(RedisTemplate<String, Object> rds, OrderService oSvc, KlineService kSvc) {
        this.rds = rds;
        this.oSvc = oSvc;
        this.kSvc = kSvc;
        this.pool = new ThreadPoolExecutor(
                4,
                10,
                3L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardOldestPolicy());
        // 从redis加载上次执行位置
        this.ts1m = (Long) rds.opsForValue().get(tsRdsKey("1m"));
        this.ts5m = (Long) rds.opsForValue().get(tsRdsKey("5m"));
        this.ts15m = (Long) rds.opsForValue().get(tsRdsKey("15m"));
        this.ts30m = (Long) rds.opsForValue().get(tsRdsKey("30m"));
    }

    @PostConstruct
    public void run() {
        new Thread(() -> {
            log.info("Kline manager start...");
            // 追加任务到最新
            while (!isLatest(ts1m, "1m")) {
                refreshTs("1m");
                pool.execute(() -> {
                    upsert1m(ts1m, ts1m + periodMilliSecs("1m"), false);
                });
            }
            while (!isLatest(ts5m, "5m")) {
                refreshTs("5m");
                pool.execute(() -> {
                    upsert5m(ts5m, ts5m + periodMilliSecs("5m"), false);
                });
            }
            while (!isLatest(ts15m, "15m")) {
                refreshTs("15m");
                pool.execute(() -> {
                    upsert15m(ts15m, ts15m + periodMilliSecs("15m"), false);
                });
            }
            while (!isLatest(ts30m, "30m")) {
                refreshTs("30m");
                pool.execute(() -> {
                    upsert30m(ts30m, ts30m + periodMilliSecs("30m"), false);
                });
            }
            // 事件触发刷新最新
            while (true) {
                refreshLatest();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    private void refreshLatest() {
        // 添加刷新1分钟k线任务
        refreshTs("1m");
        pool.execute(() -> {
            upsert1m(ts1m, ts1m + periodMilliSecs("1m"), true);
        });
        // 添加刷新5分钟k线任务
        refreshTs("5m");
        pool.execute(() -> {
            upsert5m(ts5m, ts5m + periodMilliSecs("5m"), true);
        });
        // 添加刷新15分钟k线任务
        refreshTs("15m");
        pool.execute(() -> {
            upsert15m(ts15m, ts15m + periodMilliSecs("15m"), true);
        });
        // 添加刷新30分钟k线任务
        refreshTs("30m");
        pool.execute(() -> {
            upsert30m(ts30m, ts30m + periodMilliSecs("30m"), true);
        });
    }

    private void upsert1m(long from, long to, boolean upSig) {
        synchronized (lock) {
            while (signal != 1) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            log.info("开始刷新1分钟k线({}-{}){}", from, to, upSig ? "最新" : "追加");
            List<Order> orders = this.oSvc.ordersByTimeRange(from, to);
            Kline c = new Kline(null, "1m", from, to, 0D, 0D, 0D, Double.MAX_VALUE, 0D);
            int oSize = orders.size();
            for (int i = 0; i < oSize; i++) {
                Order o = orders.get(i);
                Double price = o.getPrice();
                if (i == 0) {
                    c.setOpen(price);
                }
                if (i == oSize - 1) {
                    c.setClose(price);
                }
                if (c.getHighest() < price) {
                    c.setHighest(price);
                }
                if (c.getLowest() > price) {
                    c.setLowest(price);
                }
                c.setVolume(c.getVolume() + o.getQuantity());
            }
            saveKline(c);
            if (upSig) {
                signal = 5;
            }
            lock.notifyAll();
        }
    }

    private void upsert5m(long from, long to, boolean upSig) {
        synchronized (lock) {
            while (signal != 5) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            log.info("开始刷新5分钟k线({}-{}){}", from, to, upSig ? "最新" : "追加");
            List<Kline> lines = this.kSvc.kLines("1m", from, to);
            Kline kl = genKlineFromKlineList("5m", from, to, lines);
            saveKline(kl);
            if (upSig) {
                signal = 15;
            }
            lock.notifyAll();
        }
    }

    private void upsert15m(long from, long to, boolean upSig) {
        synchronized (lock) {
            while (signal != 15) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            log.info("开始刷新15分钟k线({}-{}){}", from, to, upSig ? "最新" : "追加");
            List<Kline> lines = this.kSvc.kLines("5m", from, to);
            Kline kl = genKlineFromKlineList("15m", from, to, lines);
            saveKline(kl);
            if (upSig) {
                signal = 30;
            }
            lock.notifyAll();
        }
    }

    private void upsert30m(long from, long to, boolean upSig) {
        synchronized (lock) {
            while (signal != 30) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            log.info("开始刷新30分钟k线({}-{}){}", from, to, upSig ? "最新" : "追加");
            List<Kline> lines = this.kSvc.kLines("15m", from, to);
            Kline kl = genKlineFromKlineList("30m", from, to, lines);
            saveKline(kl);
            if (upSig) {
                signal = 1;
            }
            lock.notifyAll();
        }
    }


    private Kline genKlineFromKlineList(String period, Long from, Long to, List<Kline> list) {
        Kline kl = new Kline(null, period, from, to, 0D, 0D, 0D, Double.MAX_VALUE, 0D);
        int oSize = list.size();
        for (int i = 0; i < oSize; i++) {
            Kline k = list.get(i);
            if (i == 0) {
                kl.setOpen(k.getOpen());
            }
            if (i == oSize - 1) {
                kl.setClose(k.getClose());
            }
            if (kl.getHighest() < k.getHighest()) {
                kl.setHighest(k.getHighest());
            }
            if (kl.getLowest() > k.getLowest()) {
                kl.setLowest(k.getLowest());
            }
            kl.setVolume(kl.getVolume() + k.getVolume());
        }
        return kl;
    }


    private void saveKline(Kline kl) {
        if (kSvc.upsertKline(kl)) {
            log.info("更新k线({}|{}-{})成功", kl.getPeriod(),kl.getFromTimestamp(),kl.getToTimestamp());
        }
    }

    // 时段对应的毫秒数
    private Long periodMilliSecs(String period) {
        return switch (period) {
            case "1m" -> 60000L;
            case "5m" -> 300000L;
            case "15m" -> 900000L;
            case "30m" -> 1800000L;
            default -> 0L;
        };
    }

    // 刷新时间戳
    private void refreshTs(String period) {
        switch (period) {
            case "1m":
                if (!isLatest(ts1m, "1m")) {
                    ts1m += periodMilliSecs("1m");
                    rds.opsForValue().set(tsRdsKey("1m"), ts1m);
                }
            case "5m":
                if (!isLatest(ts5m, "5m")) {
                    ts5m += periodMilliSecs("5m");
                    rds.opsForValue().set(tsRdsKey("5m"), ts5m);
                }
            case "15":
                if (!isLatest(ts15m, "15m")) {
                    ts15m += periodMilliSecs("15m");
                    rds.opsForValue().set(tsRdsKey("15m"), ts15m);
                }
            case "30m":
                if (!isLatest(ts30m, "30m")) {
                    ts30m += periodMilliSecs("30m");
                    rds.opsForValue().set(tsRdsKey("30m"), ts30m);
                }
        }
    }


    private String tsRdsKey(String period) {
        return "ts" + period;
    }

    // 是否是当前最新的时间段
    private boolean isLatest(Long ts, String period) {
        return (System.currentTimeMillis() - ts) <= periodMilliSecs(period);
    }
}
