package com.cardmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "店员端/后台登录请求")
public class StaffLoginRequest {

    @Schema(description = "登录手机号")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Schema(description = "登录密码")
    @NotBlank(message = "密码不能为空")
    private String password;
}
