package com.milo.kline.demo.dubbo.provider.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.milo.kline.demo.api.resp.KlineItem;
import com.milo.kline.demo.api.req.KlineListRequest;
import com.milo.kline.demo.api.resp.KlineListResponse;
import com.milo.kline.demo.api.IKlineService;
import com.milo.kline.demo.dubbo.provider.entity.Kline;
import com.milo.kline.demo.dubbo.provider.mapper.KlineMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@DubboService
@Service
public class KlineService implements IKlineService {
    @Resource(type = KlineMapper.class)
    KlineMapper km;

    @Autowired
    RedisTemplate<String, Object> rds;


    @Override
    public KlineListResponse klineList(KlineListRequest req) {
        List<Kline> lines = kLines(req.getPeriod(), req.getFromTimestamp(), req.getToTimestamp());
        List<KlineItem> items = new ArrayList<>(req.getLimit());
        for (int i = 0; i < req.getLimit(); i++) {
            items.add(lines.get(i).toItem());
        }
        return new KlineListResponse(0, "success", items);
    }


    public List<Kline> kLines(String period, Long from, Long to) {
        // 从redis获取
        Set<Object> lines = rds.opsForZSet().rangeByScore("kline" + period, from, to);
        HashMap<Long, Kline> m = new HashMap<>();
        if (lines != null) {
            for (Object l : lines) {
                Kline kl = (Kline) l;
                m.put(kl.getFromTimestamp(), kl);
            }
        }
        List<Kline> ret = new ArrayList<>();
        for (Long i = from; i <= to; i += periodMilliSecs(period)) {
            if (m.containsKey(i)) {
                ret.add(m.get(i));
            } else {
                // redis不存在的从数据库加载
                List<Kline> items = klineListFromDB(period, i, i + periodMilliSecs(period));
                ret.addAll(items);
            }
        }
        return ret;
    }

    public boolean upsertKline(Kline kl) {
        Wrapper<Kline> wrapper = Wrappers.<Kline>query().eq("period", kl.getPeriod()).eq("from_timestamp", kl.getFromTimestamp());
        long count = km.selectCount(wrapper);
        int affected;
        if (count < 1) {
            affected = km.insert(kl);
        } else {
            affected = km.update(kl, wrapper);
        }
        if (affected>0) {
            upsertRedisCache(kl);
        }
        return affected>0;
    }


    private List<Kline> klineListFromDB(String period, Long from, Long to) {
        List<Kline> list = km.selectList(Wrappers.<Kline>query().eq("period", period).ge("from_timestamp", from).le("to_timestamp", to).orderByDesc("id"));
        for (Kline kl : list) {
            upsertKline(kl);
        }
        return list;
    }

    // 更新redis缓存
    private void upsertRedisCache(Kline kl) {
        rds.opsForZSet().removeRangeByScore(kl.rdsZSetKey(), kl.getFromTimestamp(), kl.getFromTimestamp());
        rds.opsForZSet().add(kl.rdsZSetKey(), kl, kl.getFromTimestamp());
    }

    private Long periodMilliSecs(String period) {
        return switch (period) {
            case "1m" -> 60000L;
            case "5m" -> 300000L;
            case "15m" -> 900000L;
            case "30m" -> 1800000L;
            default -> 0L;
        };
    }

}
