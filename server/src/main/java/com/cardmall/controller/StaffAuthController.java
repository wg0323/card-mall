package com.cardmall.controller;

import com.cardmall.common.LoginResult;
import com.cardmall.common.Result;
import com.cardmall.common.TokenPair;
import com.cardmall.dto.RefreshTokenRequest;
import com.cardmall.dto.StaffLoginRequest;
import com.cardmall.entity.Staff;
import com.cardmall.security.LoginType;
import com.cardmall.service.StaffService;
import com.cardmall.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "店员端-登录")
@RestController
@RequestMapping("/api/staff/auth")
@RequiredArgsConstructor
public class StaffAuthController {

    private final StaffService staffService;

    private final TokenService tokenService;

    @Operation(summary = "店员登录")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody @Valid StaffLoginRequest request) {
        Staff staff = staffService.authenticate(request.getPhone(), request.getPassword());
        return Result.ok(tokenService.issue(staff.getId(), staff.getName(), LoginType.STAFF, staff.getRole()));
    }

    @Operation(summary = "刷新令牌")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/refresh")
    public Result<TokenPair> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return Result.ok(tokenService.rotate(request.getRefreshToken(), LoginType.STAFF));
    }
}
