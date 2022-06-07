package com.milo.kline.demo.api.req;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class KlineListRequest implements Serializable {
    private String period;          // k线类型 eg: 1m 5m
    private Long fromTimestamp;     // 起始时间戳(ms)
    private Long toTimestamp;       // 截止时间戳(ms)
    private Integer limit;          // 限制条数
}
