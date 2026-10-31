package com.cardmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
@Schema(description = "卡产品保存请求（新建/编辑共用）")
public class CardProductSaveRequest {

    @Schema(description = "卡名称，如 洗车5次卡")
    @NotBlank(message = "卡名称不能为空")
    private String name;

    @Schema(description = "计量方式：1-计次型，2-期限型")
    @NotNull(message = "计量方式不能为空")
    @Min(value = 1, message = "计量方式只能是 1 或 2")
    @Max(value = 2, message = "计量方式只能是 1 或 2")
    private Integer cardType;

    @Schema(description = "营销标签：服务卡/体验卡/套餐卡/月卡，仅用于展示")
    private String bizTag;

    @Schema(description = "售价")
    @NotNull(message = "售价不能为空")
    @DecimalMin(value = "0.01", message = "售价必须大于 0")
    private BigDecimal price;

    @Schema(description = "单次原价，退款折算依据；计次型必填且大于 0，期限型传 0")
    private BigDecimal unitPrice;

    @Schema(description = "总次数，仅计次型有效，期限型传 0")
    private Integer totalTimes;

    @Schema(description = "购买后有效天数")
    @NotNull(message = "有效天数不能为空")
    @Min(value = 1, message = "有效天数必须大于 0")
    private Integer validDays;

    @Schema(description = "卡封面图地址")
    private String coverImage;

    @Schema(description = "适用服务说明，M1 为纯文案")
    private String applicableService;

    @Schema(description = "购买须知")
    private String purchaseNotice;

    @Schema(description = "状态：0-下架，1-上架")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "排序值，越大越靠前")
    private Integer sort;
}
