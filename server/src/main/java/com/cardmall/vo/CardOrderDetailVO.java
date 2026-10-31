package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "订单详情：订单 + 支付流水")
public class CardOrderDetailVO {

    @Schema(description = "订单信息")
    private CardOrderVO order;

    @Schema(description = "支付流水，未支付时为空列表")
    private List<PayTransactionVO> payTransactions;
}
