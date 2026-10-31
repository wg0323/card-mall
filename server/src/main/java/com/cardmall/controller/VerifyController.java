package com.cardmall.controller;

import com.cardmall.common.Result;
import com.cardmall.dto.VerifyConfirmRequest;
import com.cardmall.dto.VerifyPreviewRequest;
import com.cardmall.security.CurrentUser;
import com.cardmall.service.VerifyService;
import com.cardmall.vo.VerifyPreviewVO;
import com.cardmall.vo.VerifyResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "店员端-核销")
@RestController
@RequestMapping("/api/staff/verify")
@RequiredArgsConstructor
public class VerifyController {

    private final VerifyService verifyService;

    @Operation(summary = "扫码预览：不扣次不删码")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/preview")
    public Result<VerifyPreviewVO> preview(@RequestBody @Valid VerifyPreviewRequest request) {
        return Result.ok(verifyService.preview(CurrentUser.id(), request.getToken()));
    }

    @Operation(summary = "确认核销")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/confirm")
    public Result<VerifyResultVO> confirm(@RequestBody @Valid VerifyConfirmRequest request) {
        Long staffId = CurrentUser.id();
        return Result.ok(verifyService.confirm(staffId, request.getToken(),
                request.getRequestId(), request.getTimes(), request.getRemark()));
    }
}
