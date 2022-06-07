package com.milo.kline.demo.dubbo.provider.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@TableName("kline")
public class Kline extends Model<Kline> {
    @TableId(value="id", type= IdType.AUTO)
    private Long id;                // 主键id
    private String period;          // k线类型 eg：1m 5m 15m
    private Long fromTimestamp;     // 时段起始时间戳(ms)
    private Long toTimestamp;       // 时段结束时间戳(ms)
    private Double open;            // 开
    private Double close;           // 收
    private Double highest;         // 高
    private Double lowest;          // 低
    private Double volume;          // 总成交量

    @Override
    public Serializable pkVal() {
        return id;
    }
}
