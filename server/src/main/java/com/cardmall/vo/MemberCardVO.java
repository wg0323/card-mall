package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "会员卡")
public class MemberCardVO {

    @Schema(description = "会员卡ID")
    private Long id;

    @Schema(description = "卡号，展示与人工查询用")
    private String cardNo;

    @Schema(description = "所属会员ID")
    private Long memberId;

    @Schema(description = "卡产品ID")
    private Long productId;

    @Schema(description = "来源订单号")
    private String orderNo;

    @Schema(description = "卡名称快照")
    private String cardName;

    @Schema(description = "计量方式：1-计次型，2-期限型")
    private Integer cardType;

    @Schema(description = "总次数，期限型为 0")
    private Integer totalTimes;

    @Schema(description = "剩余次数，期限型为 0")
    private Integer remainingTimes;

    @Schema(description = "生效时间")
    private LocalDateTime validStart;

    @Schema(description = "失效时间")
    private LocalDateTime validEnd;

    @Schema(description = "状态：1-正常，2-已用完，3-已过期，4-已冻结")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
}
