package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "门店信息")
public class StoreVO {

    @Schema(description = "门店ID")
    private Long id;

    @Schema(description = "门店名称")
    private String name;

    @Schema(description = "Logo 地址")
    private String logo;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "营业时间")
    private String businessHours;
}
