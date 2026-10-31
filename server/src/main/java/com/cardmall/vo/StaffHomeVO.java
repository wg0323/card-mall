package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "店员端首页：门店名 + 今日核销数")
public class StaffHomeVO {

    @Schema(description = "门店名称")
    private String storeName;

    @Schema(description = "今日核销次数")
    private Long todayVerifyCount;
}
