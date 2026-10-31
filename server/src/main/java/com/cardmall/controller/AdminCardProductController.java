package com.cardmall.controller;

import com.cardmall.common.PageResult;
import com.cardmall.common.Result;
import com.cardmall.converter.CardProductConverter;
import com.cardmall.dto.CardProductSaveRequest;
import com.cardmall.service.CardProductService;
import com.cardmall.vo.CardProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "后台-卡产品")
@RestController
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
public class AdminCardProductController {

    private final CardProductService cardProductService;

    @Operation(summary = "卡产品分页")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/page")
    public Result<PageResult<CardProductVO>> page(
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(cardProductService.pageAdmin(pageNum, pageSize).convert(CardProductConverter::toVO));
    }

    @Operation(summary = "新建卡产品")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping
    public Result<Long> create(@RequestBody @Valid CardProductSaveRequest request) {
        return Result.ok(cardProductService.create(request));
    }

    @Operation(summary = "编辑卡产品")
    @ApiResponse(responseCode = "200", description = "成功")
    @PutMapping("/{id}")
    public Result<Void> update(@Parameter(description = "卡产品ID") @PathVariable Long id,
                               @RequestBody @Valid CardProductSaveRequest request) {
        cardProductService.update(id, request);
        return Result.ok();
    }

    @Operation(summary = "上架/下架")
    @ApiResponse(responseCode = "200", description = "成功")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@Parameter(description = "卡产品ID") @PathVariable Long id,
                                     @Parameter(description = "状态：0-下架，1-上架") @RequestParam Integer status) {
        cardProductService.updateStatus(id, status);
        return Result.ok();
    }
}
