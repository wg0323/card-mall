package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "会员详情：基本信息、持卡、核销记录、订单")
public class MemberDetailVO {

    @Schema(description = "会员信息")
    private MemberVO member;

    @Schema(description = "持卡列表")
    private List<MemberCardVO> cards;

    @Schema(description = "核销记录，最近的在前")
    private List<VerifyRecordVO> verifies;

    @Schema(description = "订单列表")
    private List<CardOrderVO> orders;
}
