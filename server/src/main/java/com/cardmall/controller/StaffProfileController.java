package com.cardmall.controller;

import com.cardmall.common.Result;
import com.cardmall.converter.StaffConverter;
import com.cardmall.dto.AvatarRequest;
import com.cardmall.security.CurrentUser;
import com.cardmall.service.StaffService;
import com.cardmall.vo.StaffVO;
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

@Tag(name = "店员端-我的")
@RestController
@RequestMapping("/api/staff/me")
@RequiredArgsConstructor
public class StaffProfileController {

    private final StaffService staffService;

    @Operation(summary = "当前登录人信息")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping
    public Result<StaffVO> me() {
        return Result.ok(StaffConverter.toVO(staffService.getProfile(CurrentUser.id())));
    }

    @Operation(summary = "修改自己的头像")
    @ApiResponse(responseCode = "200", description = "成功")
    @PutMapping("/avatar")
    public Result<Void> updateAvatar(@RequestBody @Valid AvatarRequest request) {
        staffService.updateAvatar(CurrentUser.id(), request.getAvatar());
        return Result.ok();
    }
}
