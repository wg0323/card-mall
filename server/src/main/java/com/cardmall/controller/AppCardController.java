package com.cardmall.controller;

import com.cardmall.common.PageResult;
import com.cardmall.common.Result;
import com.cardmall.converter.CardRecordConverter;
import com.cardmall.converter.MemberCardConverter;
import com.cardmall.entity.MemberCard;
import com.cardmall.security.CurrentUser;
import com.cardmall.service.MemberCardService;
import com.cardmall.service.VerifyCodeService;
import com.cardmall.service.VerifyQueryService;
import com.cardmall.vo.CardDetailVO;
import com.cardmall.vo.CodeResult;
import com.cardmall.vo.MemberCardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "会员端-卡包")
@RestController
@RequestMapping("/api/app/card")
@RequiredArgsConstructor
public class AppCardController {

    private static final int RECENT_RECORD_LIMIT = 20;

    private final MemberCardService memberCardService;

    private final VerifyCodeService verifyCodeService;

    private final VerifyQueryService verifyQueryService;

    @Operation(summary = "我的卡包")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/page")
    public Result<PageResult<MemberCardVO>> page(
            @Parameter(description = "卡状态：1-正常，2-已用完，3-已过期，4-已冻结，不传为全部")
            @RequestParam(required = false) Integer status,
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        Long memberId = CurrentUser.id();
        return Result.ok(memberCardService.pageByMember(memberId, status, pageNum, pageSize)
                .convert(MemberCardConverter::toVO));
    }

    @Operation(summary = "卡详情 + 最近使用记录")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/{id}")
    public Result<CardDetailVO> detail(@Parameter(description = "会员卡ID") @PathVariable Long id) {
        Long memberId = CurrentUser.id();
        // 详情只校验归属：已用完/已过期的卡也要能回看记录
        MemberCard card = memberCardService.checkOwner(memberId, id);
        return Result.ok(new CardDetailVO(MemberCardConverter.toVO(card),
                CardRecordConverter.toVOList(verifyQueryService.listRecentByCard(id, RECENT_RECORD_LIMIT))));
    }

    @Operation(summary = "生成核销码")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping("/{id}/verify-code")
    public Result<CodeResult> issueCode(@Parameter(description = "会员卡ID") @PathVariable Long id) {
        Long memberId = CurrentUser.id();
        return Result.ok(verifyCodeService.issue(memberId, id));
    }
}
