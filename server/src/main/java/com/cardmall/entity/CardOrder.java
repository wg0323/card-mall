package com.cardmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("card_order")
public class CardOrder {

    public static final int STATUS_UNPAID = 0;
    public static final int STATUS_PAID = 1;
    public static final int STATUS_CLOSED = 2;
    public static final int STATUS_REFUNDED = 3;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long storeId;

    private Long memberId;

    private Long productId;

    private String productName;

    private BigDecimal amount;

    private String payChannel;

    /** 0-待支付，1-已支付，2-已关闭，3-已退款 */
    private Integer payStatus;

    private LocalDateTime paidTime;

    private LocalDateTime closeTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
