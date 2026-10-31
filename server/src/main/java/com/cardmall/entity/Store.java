package com.cardmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("store")
public class Store {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String logo;

    private String phone;

    private String address;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String businessHours;

    /** 0-停用，1-启用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
