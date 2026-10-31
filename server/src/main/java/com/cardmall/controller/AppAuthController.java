package com.cardmall.controller;

import com.cardmall.common.LoginResult;
import com.cardmall.common.Result;
import com.cardmall.common.TokenPair;
import com.cardmall.dto.AppLoginRequest;
import com.cardmall.dto.RefreshTokenRequest;
import com.cardmall.security.LoginType;
import com.cardmall.service.MemberService;
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

@Tag(name = "会员端-登录")
@RestController
@RequestMapping("/api/app/auth")
@RequiredArgsConstructor
public class AppAuthController {

    private final MemberService memberService;

    private final TokenService tokenService;

    @Operation(summary = "小程序登录，用 wx.login 的 code 换 token")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody @Valid AppLoginRequest request) {
        return Result.ok(memberService.loginByCode(request.getCode()));
    }

    @Operation(summary = "刷新令牌")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/refresh")
    public Result<TokenPair> refresh(@RequestBody @Valid RefreshTokenRequest request) {
        return Result.ok(tokenService.rotate(request.getRefreshToken(), LoginType.MEMBER));
    }
}
