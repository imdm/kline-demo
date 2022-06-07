package com.milo.kline.demo.dubbo.consumer.dto.dio;

import com.milo.kline.demo.api.req.KlineListRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Range;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KlineListReq {
    @NotBlank
    private String period;          // k线类型 eg: 1m 5m
    private Long from;              // 起始时间戳(ms)
    private Long to;                // 截止时间戳(ms)
    @Min(value = 1)
    private Integer limit;          // 限制条数

    public KlineListRequest toDubboReq() {
        return new KlineListRequest(period, from, to, limit);
    }
}
