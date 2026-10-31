package com.cardmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(description = "创建订单请求")
public class CreateOrderRequest {

    @Schema(description = "卡产品ID")
    @NotNull(message = "productId 不能为空")
    private Long productId;
}
