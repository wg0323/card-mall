package com.cardmall.vo;

import com.cardmall.entity.VerifyRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "核销结果")
public class VerifyResultVO {

    @Schema(description = "核销单号")
    private String recordNo;

    @Schema(description = "会员卡ID")
    private Long cardId;

    @Schema(description = "卡名称快照")
    private String cardName;

    @Schema(description = "本次扣减次数，期限型为 0")
    private Integer verifyTimes;

    @Schema(description = "核销后余次，期限型为空")
    private Integer remainingTimes;

    @Schema(description = "核销时间")
    private LocalDateTime verifyTime;

    public static VerifyResultVO of(VerifyRecord record) {
        VerifyResultVO vo = new VerifyResultVO();
        vo.setRecordNo(record.getRecordNo());
        vo.setCardId(record.getMemberCardId());
        vo.setVerifyTimes(record.getVerifyTimes());
        vo.setRemainingTimes(record.getRemainingAfter());
        vo.setVerifyTime(record.getVerifyTime());
        return vo;
    }
}
