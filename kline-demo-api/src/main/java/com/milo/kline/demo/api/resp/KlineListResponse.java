package com.milo.kline.demo.api.resp;

import com.milo.kline.demo.api.resp.KlineItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class KlineListResponse implements Serializable {
    private Integer result;
    private String msg;
    private List<KlineItem> data;
}
