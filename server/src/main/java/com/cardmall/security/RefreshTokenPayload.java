package com.cardmall.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 解析出的刷新令牌：jti 用来查服务端白名单，user 用来重签 access token。
 */
@Getter
@AllArgsConstructor
public class RefreshTokenPayload {

    private final String jti;

    private final LoginUser user;
}
