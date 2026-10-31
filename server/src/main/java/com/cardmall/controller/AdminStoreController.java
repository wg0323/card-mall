package com.cardmall.controller;

import com.cardmall.common.Result;
import com.cardmall.converter.StoreConverter;
import com.cardmall.dto.StoreSaveRequest;
import com.cardmall.service.StoreService;
import com.cardmall.vo.StoreVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "后台-门店设置")
@RestController
@RequestMapping("/api/admin/store")
@RequiredArgsConstructor
public class AdminStoreController {

    private final StoreService storeService;

    @Operation(summary = "读取门店信息")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping
    public Result<StoreVO> get() {
        return Result.ok(StoreConverter.toVO(storeService.getMainStore()));
    }

    @Operation(summary = "保存门店信息")
    @ApiResponse(responseCode = "200", description = "成功")
    @PutMapping
    public Result<Void> update(@RequestBody @Valid StoreSaveRequest request) {
        storeService.saveStore(request);
        return Result.ok();
    }
}
