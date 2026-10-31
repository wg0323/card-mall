package com.cardmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("pay_transaction")
public class PayTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private String channel;

    /** 渠道支付单号，回调幂等依据 */
    private String transactionId;

    private BigDecimal amount;

    /** 0-处理中，1-成功，2-失败 */
    private Integer status;

    private LocalDateTime finishTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

