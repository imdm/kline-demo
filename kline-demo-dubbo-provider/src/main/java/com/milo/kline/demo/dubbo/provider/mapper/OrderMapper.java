package com.milo.kline.demo.dubbo.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.milo.kline.demo.dubbo.provider.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
