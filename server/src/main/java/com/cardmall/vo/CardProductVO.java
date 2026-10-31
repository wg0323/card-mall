package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "卡产品")
public class CardProductVO {

    @Schema(description = "卡产品ID")
    private Long id;

    @Schema(description = "卡名称，如 洗车5次卡")
    private String name;

    @Schema(description = "计量方式：1-计次型，2-期限型")
    private Integer cardType;

    @Schema(description = "营销标签：服务卡/体验卡/套餐卡/月卡，仅用于展示")
    private String bizTag;

    @Schema(description = "售价")
    private BigDecimal price;

    @Schema(description = "单次原价，退款折算依据；期限型为 0")
    private BigDecimal unitPrice;

    @Schema(description = "总次数，期限型为 0")
    private Integer totalTimes;

    @Schema(description = "购买后有效天数")
    private Integer validDays;

    @Schema(description = "卡封面图地址")
    private String coverImage;

    @Schema(description = "适用服务说明")
    private String applicableService;

    @Schema(description = "购买须知")
    private String purchaseNotice;

    @Schema(description = "状态：0-下架，1-上架")
    private Integer status;

    @Schema(description = "排序值，越大越靠前")
    private Integer sort;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
}
