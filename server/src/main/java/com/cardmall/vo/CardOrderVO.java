package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "卡订单")
public class CardOrderVO {

    @Schema(description = "订单ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "下单门店ID")
    private Long storeId;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员昵称，仅后台查询联表返回；会员端为 null")
    private String memberNickname;

    @Schema(description = "会员手机号，仅后台查询联表返回；会员端为 null")
    private String memberPhone;

    @Schema(description = "卡产品ID")
    private Long productId;

    @Schema(description = "卡名称快照")
    private String productName;

    @Schema(description = "实付金额")
    private BigDecimal amount;

    @Schema(description = "支付渠道：MOCK-模拟，WXPAY-微信支付")
    private String payChannel;

    @Schema(description = "支付状态：0-待支付，1-已支付，2-已关闭，3-已退款")
    private Integer payStatus;

    @Schema(description = "支付时间，未支付时为空")
    private LocalDateTime paidTime;

    @Schema(description = "关闭时间，未关闭时为空")
    private LocalDateTime closeTime;

    @Schema(description = "下单时间")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
}
