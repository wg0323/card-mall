package com.cardmall.controller;

import com.cardmall.common.PageResult;
import com.cardmall.common.Result;
import com.cardmall.converter.CardOrderConverter;
import com.cardmall.converter.MemberCardConverter;
import com.cardmall.converter.MemberConverter;
import com.cardmall.entity.MemberInfo;
import com.cardmall.service.CardOrderService;
import com.cardmall.service.MemberCardService;
import com.cardmall.service.MemberService;
import com.cardmall.service.VerifyQueryService;
import com.cardmall.vo.AdminMemberVO;
import com.cardmall.vo.CardOrderDetailVO;
import com.cardmall.vo.CardOrderVO;
import com.cardmall.vo.MemberDetailVO;
import com.cardmall.vo.VerifyRecordVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Tag(name = "后台-查询")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminQueryController {

    /** 会员详情里的核销记录只展示最近 50 条，完整历史在核销记录页 */
    private static final int MEMBER_DETAIL_VERIFY_LIMIT = 50;

    private final MemberService memberService;

    private final MemberCardService memberCardService;

    private final CardOrderService cardOrderService;

    private final VerifyQueryService verifyQueryService;

    @Operation(summary = "会员分页，手机号精确、昵称右模糊")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/member/page")
    public Result<PageResult<AdminMemberVO>> memberPage(
            @Parameter(description = "关键词：手机号精确匹配、昵称右模糊") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(memberService.pageForAdmin(keyword, pageNum, pageSize));
    }

    @Operation(summary = "会员详情：持卡、核销记录、订单")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/member/{id}")
    public Result<MemberDetailVO> memberDetail(@Parameter(description = "会员ID") @PathVariable Long id) {
        MemberInfo member = memberService.getById(id);
        if (member == null) {
            return Result.ok(null);
        }
        MemberDetailVO vo = new MemberDetailVO();
        vo.setMember(MemberConverter.toVO(member));
        vo.setCards(MemberCardConverter.toVOList(memberCardService.listByMember(id)));
        vo.setVerifies(verifyQueryService.listByMember(id, MEMBER_DETAIL_VERIFY_LIMIT));
        vo.setOrders(CardOrderConverter.toVOList(cardOrderService.listByMember(id)));
        return Result.ok(vo);
    }

    @Operation(summary = "订单分页")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/order/page")
    public Result<PageResult<CardOrderVO>> orderPage(
            @Parameter(description = "支付状态：0-待支付，1-已支付，2-已关闭，3-已退款") @RequestParam(required = false) Integer payStatus,
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(cardOrderService.pageForAdmin(payStatus, pageNum, pageSize));
    }

    @Operation(summary = "订单详情：订单 + 支付流水")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/order/{orderNo}")
    public Result<CardOrderDetailVO> orderDetail(@Parameter(description = "订单号") @PathVariable String orderNo) {
        return Result.ok(cardOrderService.getDetailForAdmin(orderNo));
    }

    @Operation(summary = "核销记录分页")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/verify/page")
    public Result<PageResult<VerifyRecordVO>> verifyPage(
            @Parameter(description = "开始日期，格式 yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "结束日期，格式 yyyy-MM-dd")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "核销状态：1-正常，2-已撤销") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(verifyQueryService.page(null, startDate, endDate, status, pageNum, pageSize));
    }
}
