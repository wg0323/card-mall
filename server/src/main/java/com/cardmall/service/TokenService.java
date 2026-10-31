package com.cardmall.service;

import com.cardmall.common.LoginResult;
import com.cardmall.common.TokenPair;
import com.cardmall.security.LoginType;

/**
 * 双令牌签发与轮换：access token 无状态短有效期，refresh token 落 Redis 白名单可撤销。
 */
public interface TokenService {

    LoginResult issue(Long userId, String name, LoginType loginType, Integer role);

    TokenPair rotate(String refreshToken, LoginType expectedType);

    void revoke(String refreshToken);
}
