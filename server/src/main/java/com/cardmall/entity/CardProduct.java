package com.cardmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("card_product")
public class CardProduct {

    public static final int TYPE_COUNT = 1;
    public static final int TYPE_PERIOD = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 1-计次型，2-期限型 */
    private Integer cardType;

    private String bizTag;

    private BigDecimal price;

    /** 单次原价，退款折算依据；期限型为 0 */
    private BigDecimal unitPrice;

    private Integer totalTimes;

    private Integer validDays;

    private String coverImage;

    private String applicableService;

    private String purchaseNotice;

    /** 0-下架，1-上架 */
    private Integer status;

    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
