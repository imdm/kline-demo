package com.milo.kline.demo.api;

import com.milo.kline.demo.api.req.KlineListRequest;
import com.milo.kline.demo.api.resp.KlineListResponse;

public interface IKlineService {
    KlineListResponse klineList(KlineListRequest req);
}
