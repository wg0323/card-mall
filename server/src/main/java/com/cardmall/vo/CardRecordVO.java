package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 卡详情页的使用记录时间轴。只给展示需要的字段，不含 requestId 等内部字段。
 */
@Data
@Schema(description = "卡使用记录（时间轴项）")
public class CardRecordVO {

    @Schema(description = "核销记录ID")
    private Long id;

    @Schema(description = "本次扣减次数，期限型为 0")
    private Integer verifyTimes;

    @Schema(description = "核销前余次，期限型为空")
    private Integer remainingBefore;

    @Schema(description = "核销后余次，期限型为空")
    private Integer remainingAfter;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "状态：1-正常，2-已撤销")
    private Integer status;

    @Schema(description = "核销时间")
    private LocalDateTime verifyTime;
}
