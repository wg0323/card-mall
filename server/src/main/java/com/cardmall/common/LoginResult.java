package com.cardmall.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "登录结果")
public class LoginResult {

    @Schema(description = "登录令牌，后续请求放在 Authorization: Bearer 头中")
    private String token;

    @Schema(description = "登录用户ID")
    private Long id;

    @Schema(description = "登录用户姓名，仅会员端为空")
    private String name;

    @Schema(description = "角色：1-老板，2-店长，3-店员；会员端为空")
    private Integer role;

    @Schema(description = "刷新令牌，access token 过期时用它换新令牌")
    private String refreshToken;
}
