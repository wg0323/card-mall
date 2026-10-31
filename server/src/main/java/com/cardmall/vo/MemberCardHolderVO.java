package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "会员及其持卡")
public class MemberCardHolderVO {

    @Schema(description = "会员信息")
    private MemberVO member;

    @Schema(description = "该会员的会员卡列表")
    private List<MemberCardVO> cards;
}
