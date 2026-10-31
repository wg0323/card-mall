package com.cardmall.controller;

import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import com.cardmall.common.LoginResult;
import com.cardmall.common.Result;
import com.cardmall.common.TokenPair;
import com.cardmall.dto.ChangePasswordRequest;
import com.cardmall.dto.RefreshTokenRequest;
import com.cardmall.dto.StaffLoginRequest;
import com.cardmall.entity.Staff;
import com.cardmall.security.CurrentUser;
import com.cardmall.security.LoginType;
import com.cardmall.service.StaffService;
import com.cardmall.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "后台-登录")
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private static final int ROLE_CLERK = 3;

    private final StaffService staffService;

    private final TokenService tokenService;

    @Operation(summary = "后台登录")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody @Valid StaffLoginRequest request) {
        Staff staff = staffService.authenticate(request.getPhone(), request.getPassword());
        // 店员只能进店员端，不能换到后台权限
        if (staff.getRole() == null || staff.getRole() == ROLE_CLERK) {
            throw new BizException(ErrorCode.STAFF_ACCOUNT_INVALID, "该账号无后台权限");
        }
        return Result.ok(tokenService.issue(staff.getId(), staff.getName(), LoginType.ADMIN, staff.getRole()));
    }

    @Operation(summary = "刷新令牌")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/refresh")
    public Result<TokenPair> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return Result.ok(tokenService.rotate(request.getRefreshToken(), LoginType.ADMIN));
    }

    @Operation(summary = "后台改密")
    @ApiResponse(responseCode = "200", description = "成功")
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        staffService.changePassword(CurrentUser.id(), request.getOldPassword(), request.getNewPassword());
        return Result.ok();
    }

    /**
     * 退出时撤销刷新令牌，access token 只能等它自己过期。
     */
    @Operation(summary = "退出登录")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        if (request != null) {
            tokenService.revoke(request.getRefreshToken());
        }
        return Result.ok();
    }
}
