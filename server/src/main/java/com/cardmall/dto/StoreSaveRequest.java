package com.cardmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

@Data
@Schema(description = "门店保存请求")
public class StoreSaveRequest {

    @Schema(description = "门店名称")
    @NotBlank(message = "门店名称不能为空")
    @Size(max = 64, message = "门店名称不能超过64字")
    private String name;

    @Schema(description = "门店 Logo 地址")
    @Size(max = 255, message = "Logo 地址过长")
    private String logo;

    @Schema(description = "联系电话")
    @Size(max = 20, message = "电话过长")
    private String phone;

    @Schema(description = "详细地址")
    @Size(max = 200, message = "地址不能超过200字")
    private String address;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "营业时间文案")
    @Size(max = 64, message = "营业时间过长")
    private String businessHours;
}
