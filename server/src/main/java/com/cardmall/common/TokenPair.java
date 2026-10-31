package com.cardmall.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "双令牌")
public class TokenPair {

    @Schema(description = "访问令牌，放在 Authorization: Bearer 头中")
    private String token;

    @Schema(description = "刷新令牌，access token 过期时用它换新令牌")
    private String refreshToken;

    @Schema(description = "访问令牌有效期，单位秒")
    private long tokenExpireIn;

    @Schema(description = "刷新令牌有效期，单位秒")
    private long refreshExpireIn;
}
