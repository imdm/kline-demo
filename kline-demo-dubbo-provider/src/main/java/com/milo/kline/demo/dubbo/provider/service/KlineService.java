package com.milo.kline.demo.dubbo.provider.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.milo.kline.demo.api.resp.KlineItem;
import com.milo.kline.demo.api.req.KlineListRequest;
import com.milo.kline.demo.api.resp.KlineListResponse;
import com.milo.kline.demo.api.IKlineService;
import com.milo.kline.demo.dubbo.provider.entity.Kline;
import com.milo.kline.demo.dubbo.provider.mapper.KlineMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@DubboService
public class KlineService implements IKlineService {
    @Resource(type = KlineMapper.class)
    KlineMapper km;

    @Cacheable(value = "klineList")
    @Override
    public KlineListResponse klineList(KlineListRequest req) {
        List<Kline> list = km.selectList(Wrappers.<Kline>query().eq("period", req.getPeriod()).ge("from_timestamp",req.getFromTimestamp()).le("to_timestamp", req.getToTimestamp()).orderByDesc("id").last(String.format("limit %d", req.getLimit())));
        List<KlineItem> items = new ArrayList<>();
        for (Kline k : list) {
            items.add(new KlineItem(k.getFromTimestamp(), k.getToTimestamp(), k.getOpen(), k.getClose(), k.getHighest(), k.getLowest(), k.getVolume()));
        }
        return new KlineListResponse(0, "success", items);
    }
}
