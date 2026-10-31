package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "核销码预览")
public class VerifyPreviewVO {

    @Schema(description = "卡ID")
    private Long cardId;

    @Schema(description = "卡号")
    private String cardNo;

    @Schema(description = "卡名")
    private String cardName;

    @Schema(description = "卡类型：1 计次，2 时效")
    private Integer cardType;

    @Schema(description = "剩余次数")
    private Integer remainingTimes;

    @Schema(description = "有效期截止")
    private LocalDateTime validEnd;

    @Schema(description = "会员昵称")
    private String memberNickname;

    @Schema(description = "会员手机号")
    private String memberPhone;

    @Schema(description = "单次可扣上限 = min(剩余次数, 单次上限)")
    private int maxTimes;
}
