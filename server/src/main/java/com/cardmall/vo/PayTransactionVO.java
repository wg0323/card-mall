package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "支付流水")
public class PayTransactionVO {

    @Schema(description = "流水ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "支付渠道：MOCK-模拟，WXPAY-微信支付")
    private String channel;

    @Schema(description = "渠道支付单号")
    private String transactionId;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "流水状态：0-处理中，1-成功，2-失败")
    private Integer status;

    @Schema(description = "完成时间")
    private LocalDateTime finishTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
