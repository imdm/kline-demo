package com.milo.kline.demo.api.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class KlineItem implements Serializable {
    private Long fromTimestamp;
    private Long toTimestamp;
    private Double open;
    private Double close;
    private Double highest;
    private Double lowest;
    private Double volume;
}

