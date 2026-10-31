package com.cardmall.controller;

import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import com.cardmall.common.Result;
import com.cardmall.dto.CreateOrderRequest;
import com.cardmall.security.CurrentUser;
import com.cardmall.service.CardOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "会员端-订单")
@RestController
@RequestMapping("/api/app/order")
@RequiredArgsConstructor
public class AppOrderController {

    private final CardOrderService cardOrderService;

    @Value("${card-mall.mock.enabled:false}")
    private boolean mockEnabled;

    @Operation(summary = "创建订单")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping
    public Result<String> create(@RequestBody @Valid CreateOrderRequest request) {
        Long memberId = CurrentUser.id();
        return Result.ok(cardOrderService.create(memberId, request.getProductId()));
    }

    @Operation(summary = "模拟支付（仅本地/测试，card-mall.mock.enabled=false 时不开放）")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/{orderNo}/mock-pay")
    public Result<Void> mockPay(@Parameter(description = "订单号") @PathVariable String orderNo) {
        if (!mockEnabled) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "模拟支付未开启");
        }
        Long memberId = CurrentUser.id();
        cardOrderService.mockPay(memberId, orderNo);
        return Result.ok();
    }
}
