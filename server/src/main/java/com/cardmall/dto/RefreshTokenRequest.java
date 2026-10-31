package com.cardmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "刷新令牌请求")
public class RefreshTokenRequest {

    @NotBlank(message = "refreshToken 不能为空")
    @Schema(description = "登录时下发的刷新令牌")
    private String refreshToken;
}
