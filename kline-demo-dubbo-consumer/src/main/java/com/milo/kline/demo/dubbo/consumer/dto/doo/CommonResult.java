package com.milo.kline.demo.dubbo.consumer.dto.doo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommonResult<T> implements Serializable {
    private Integer code;
    private String message;
    private T data;
}