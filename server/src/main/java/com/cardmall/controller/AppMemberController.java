package com.cardmall.controller;

import com.cardmall.common.Result;
import com.cardmall.converter.MemberConverter;
import com.cardmall.dto.BindPhoneRequest;
import com.cardmall.dto.MemberProfileUpdateRequest;
import com.cardmall.security.CurrentUser;
import com.cardmall.service.MemberService;
import com.cardmall.vo.MemberVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "会员端-我的")
@RestController
@RequestMapping("/api/app")
@RequiredArgsConstructor
public class AppMemberController {

    private final MemberService memberService;

    @Operation(summary = "我的信息")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/member/profile")
    public Result<MemberVO> profile() {
        return Result.ok(MemberConverter.toVO(memberService.getProfile(CurrentUser.id())));
    }

    @Operation(summary = "绑定手机号")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/auth/phone")
    public Result<String> bindPhone(@RequestBody @Valid BindPhoneRequest request) {
        return Result.ok(memberService.bindPhone(CurrentUser.id(), request.getCode()));
    }

    @Operation(summary = "修改我的头像与昵称")
    @ApiResponse(responseCode = "200", description = "成功")
    @PutMapping("/member/profile")
    public Result<Void> updateProfile(@RequestBody @Valid MemberProfileUpdateRequest request) {
        memberService.updateProfile(CurrentUser.id(), request);
        return Result.ok();
    }
}
