package com.cardmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "小程序登录请求")
public class AppLoginRequest {

    @Schema(description = "wx.login 返回的 code，用于换 openid")
    @NotBlank(message = "code 不能为空")
    private String code;
}
