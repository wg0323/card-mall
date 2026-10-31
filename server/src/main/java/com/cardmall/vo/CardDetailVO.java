package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Schema(description = "会员卡详情 + 最近使用记录")
public class CardDetailVO {

    @Schema(description = "会员卡信息")
    private MemberCardVO card;

    @Schema(description = "最近 20 条使用记录")
    private List<CardRecordVO> records;
}
