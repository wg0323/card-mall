package com.cardmall.controller;

import com.cardmall.common.PageResult;
import com.cardmall.common.Result;
import com.cardmall.converter.CardOrderConverter;
import com.cardmall.security.CurrentUser;
import com.cardmall.service.CardOrderService;
import com.cardmall.vo.CardOrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "会员端-订单查询")
@RestController
@RequestMapping("/api/app/order")
@RequiredArgsConstructor
public class AppOrderQueryController {

    private final CardOrderService cardOrderService;

    @Operation(summary = "我的订单分页")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/page")
    public Result<PageResult<CardOrderVO>> page(
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(cardOrderService.pageByMember(CurrentUser.id(), pageNum, pageSize)
                .convert(CardOrderConverter::toVO));
    }

    @Operation(summary = "订单详情，只能看自己的")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/{orderNo}")
    public Result<CardOrderVO> detail(@Parameter(description = "订单号") @PathVariable String orderNo) {
        Long memberId = CurrentUser.id();
        return Result.ok(CardOrderConverter.toVO(cardOrderService.getByOrderNoForMember(memberId, orderNo)));
    }
}
