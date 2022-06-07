package com.milo.kline.demo.dubbo.consumer.dto.doo;

import com.milo.kline.demo.api.resp.KlineItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OneKline implements Serializable {
    private Long fromTimestamp;
    private Long toTimestamp;
    private Double open;
    private Double close;
    private Double highest;
    private Double lowest;
    private Double volume;

    public OneKline(KlineItem k) {
        fromTimestamp = k.getFromTimestamp();
        toTimestamp = k.getToTimestamp();
        open = k.getOpen();
        close = k.getClose();
        highest = k.getHighest();
        lowest = k.getLowest();
        volume = k.getVolume();
    }
}
