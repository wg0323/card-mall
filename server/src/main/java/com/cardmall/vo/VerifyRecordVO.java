package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "核销记录")
public class VerifyRecordVO {

    @Schema(description = "核销记录ID")
    private Long id;

    @Schema(description = "核销单号")
    private String recordNo;

    @Schema(description = "会员卡ID")
    private Long cardId;

    @Schema(description = "卡名称快照")
    private String cardName;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员昵称")
    private String memberNickname;

    @Schema(description = "会员手机号")
    private String memberPhone;

    @Schema(description = "核销员工ID")
    private Long staffId;

    @Schema(description = "核销员工姓名")
    private String staffName;

    @Schema(description = "本次扣减次数，期限型为 0")
    private Integer verifyTimes;

    @Schema(description = "核销前余次，期限型为空")
    private Integer remainingBefore;

    @Schema(description = "核销后余次，期限型为空")
    private Integer remainingAfter;

    @Schema(description = "状态：1-正常，2-已撤销")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "核销时间")
    private LocalDateTime verifyTime;
}
