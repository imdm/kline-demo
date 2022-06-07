package com.milo.kline.demo.dubbo.consumer.service.impl;

import com.milo.kline.demo.api.IKlineService;
import com.milo.kline.demo.api.resp.KlineItem;
import com.milo.kline.demo.api.resp.KlineListResponse;
import com.milo.kline.demo.dubbo.consumer.dto.dio.KlineListReq;
import com.milo.kline.demo.dubbo.consumer.dto.doo.CommonResult;
import com.milo.kline.demo.dubbo.consumer.dto.doo.OneKline;
import com.milo.kline.demo.dubbo.consumer.service.KlineService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service

public class KlineServiceImpl implements KlineService {
    @DubboReference
    private IKlineService dubboSvc;

    public CommonResult<List<OneKline>> KlineList(KlineListReq req) {
        KlineListResponse resp = dubboSvc.klineList(req.toDubboReq());
        List<OneKline> list = new ArrayList<>();
        if (resp.getResult() == 0 && resp.getData() != null) {
            for (KlineItem item : resp.getData()) {
                list.add(new OneKline(item));
            }
        }
        return new CommonResult<>(resp.getResult(), resp.getMsg(), list);
    }
}

