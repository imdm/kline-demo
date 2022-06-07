package com.milo.kline.demo.dubbo.consumer.controller;

import com.milo.kline.demo.dubbo.consumer.dto.dio.KlineListReq;
import com.milo.kline.demo.dubbo.consumer.dto.doo.CommonResult;
import com.milo.kline.demo.dubbo.consumer.dto.doo.OneKline;
import com.milo.kline.demo.dubbo.consumer.service.KlineService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@Slf4j
public class KlineController {
    @Resource
    private KlineService svc;


    @GetMapping(value = "/kline")
    public CommonResult<List<OneKline>> getPaymentById(@Validated KlineListReq req) {
        if (req.getTo() == null || req.getTo()<=0) {
            req.setTo(toTimestamp(beginOfMinute()));
        }
        if (req.getFrom() == null) {
            req.setFrom(0L);
        }
        if (req.getFrom()>req.getTo()) {
            return new CommonResult<>(-1,"请选择有效的起止时间!", null);
        }
        if (req.getLimit() == null) {
            req.setLimit(50);
        }
        return svc.KlineList(req);
    }

    private LocalDateTime beginOfMinute() {
        LocalDateTime now = LocalDateTime.now();
        return LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), now.getHour(), now.getMinute());
    }

    private Long toTimestamp(LocalDateTime t) {
        return t.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

}