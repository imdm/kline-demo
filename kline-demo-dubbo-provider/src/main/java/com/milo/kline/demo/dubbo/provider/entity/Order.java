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
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("`order`")
public class Order extends Model<Order> {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;            // 主键id
    private Double price;       // 成交价
    private Double quantity;    // 成交数量
    private Long timestamp;     // 时间戳(ms)

    @Override
    public Serializable pkVal() {
        return id;
    }
}
