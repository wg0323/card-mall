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
import com.cardmall.service.StoreService;
import com.cardmall.service.VerifyQueryService;
import com.cardmall.vo.MemberCardHolderVO;
import com.cardmall.vo.MemberCardVO;
import com.cardmall.vo.MemberDetailVO;
import com.cardmall.vo.StaffHomeVO;
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

@Tag(name = "店员端-查询")
@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffQueryController {

    private final VerifyQueryService verifyQueryService;

    private final StoreService storeService;

    private final MemberService memberService;

    private final MemberCardService memberCardService;

    private final CardOrderService cardOrderService;

    @Operation(summary = "店员首页：门店名 + 今日核销数")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/home")
    public Result<StaffHomeVO> home() {
        StaffHomeVO vo = new StaffHomeVO();
        vo.setStoreName(storeService.getMainStore().getName());
        vo.setTodayVerifyCount(verifyQueryService.todayCount(storeService.getMainStore().getId()));
        return Result.ok(vo);
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
        return Result.ok(verifyQueryService.page(storeService.getMainStore().getId(),
                startDate, endDate, status, pageNum, pageSize));
    }

    @Operation(summary = "核销记录详情")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/verify/{id}")
    public Result<VerifyRecordVO> verifyDetail(@Parameter(description = "核销记录ID") @PathVariable Long id) {
        return Result.ok(verifyQueryService.getDetail(id));
    }

    @Operation(summary = "按手机号精确查会员及其持卡")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/member/search")
    public Result<MemberCardHolderVO> searchMember(@Parameter(description = "会员手机号，精确匹配") @RequestParam String phone) {
        MemberInfo member = memberService.findByPhone(phone);
        if (member == null) {
            return Result.ok(null);
        }
        MemberCardHolderVO vo = new MemberCardHolderVO();
        vo.setMember(MemberConverter.toVO(member));
        vo.setCards(MemberCardConverter.toVOList(memberCardService.listByMember(member.getId())));
        return Result.ok(vo);
    }

    @Operation(summary = "会员详情：资料 + 持卡 + 订单")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/member/detail")
    public Result<MemberDetailVO> memberDetail(@Parameter(description = "会员手机号，精确匹配") @RequestParam String phone) {
        MemberInfo member = memberService.findByPhone(phone);
        if (member == null) {
            return Result.ok(null);
        }
        MemberDetailVO vo = new MemberDetailVO();
        vo.setMember(MemberConverter.toVO(member));
        vo.setCards(MemberCardConverter.toVOList(memberCardService.listByMember(member.getId())));
        vo.setOrders(CardOrderConverter.toVOList(cardOrderService.listByMember(member.getId())));
        return Result.ok(vo);
    }

    @Operation(summary = "按卡查详情，用于手工核销")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/card/{id}")
    public Result<MemberCardVO> cardDetail(@Parameter(description = "会员卡ID") @PathVariable Long id) {
        return Result.ok(MemberCardConverter.toVO(memberCardService.getCardById(id)));
    }
}
