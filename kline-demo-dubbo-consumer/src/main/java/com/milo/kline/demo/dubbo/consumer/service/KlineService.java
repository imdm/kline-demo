package com.milo.kline.demo.dubbo.consumer.service;

import com.milo.kline.demo.dubbo.consumer.dto.dio.KlineListReq;
import com.milo.kline.demo.dubbo.consumer.dto.doo.CommonResult;
import com.milo.kline.demo.dubbo.consumer.dto.doo.OneKline;
import java.util.List;

public interface KlineService {
    CommonResult<List<OneKline>> KlineList(KlineListReq req);
}
