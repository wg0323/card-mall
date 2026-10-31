package com.cardmall.controller;

import com.cardmall.common.Result;
import com.cardmall.converter.CardProductConverter;
import com.cardmall.converter.StoreConverter;
import com.cardmall.service.CardProductService;
import com.cardmall.service.StoreService;
import com.cardmall.vo.AppHomeVO;
import com.cardmall.vo.CardProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "会员端-首页")
@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppHomeController {

    private final StoreService storeService;

    private final CardProductService cardProductService;

    @Operation(summary = "首页：门店信息 + 在售卡")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/home")
    public Result<AppHomeVO> home() {
        AppHomeVO vo = new AppHomeVO();
        vo.setStore(StoreConverter.toVO(storeService.getMainStore()));
        vo.setProducts(CardProductConverter.toVOList(cardProductService.listOnSale()));
        return Result.ok(vo);
    }

    @Operation(summary = "卡详情：仅返回在售卡")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/product/{id}")
    public Result<CardProductVO> product(@Parameter(description = "卡产品ID") @PathVariable Long id) {
        return Result.ok(CardProductConverter.toVO(cardProductService.getOnSaleOrThrow(id)));
    }
}
