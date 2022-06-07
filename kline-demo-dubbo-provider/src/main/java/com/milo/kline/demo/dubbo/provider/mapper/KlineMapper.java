package com.milo.kline.demo.dubbo.provider.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.milo.kline.demo.dubbo.provider.entity.Kline;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface KlineMapper extends BaseMapper<Kline> {
}
