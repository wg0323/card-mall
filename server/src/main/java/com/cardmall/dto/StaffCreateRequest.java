package com.cardmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "新增员工请求")
public class StaffCreateRequest {

    @Schema(description = "员工姓名")
    @NotBlank(message = "姓名不能为空")
    private String name;

    @Schema(description = "登录手机号，全局唯一")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Schema(description = "角色：1-老板，2-店长，3-店员")
    @NotNull(message = "角色不能为空")
    @Min(value = 1, message = "角色只能是 1、2、3")
    @Max(value = 3, message = "角色只能是 1、2、3")
    private Integer role;
}
