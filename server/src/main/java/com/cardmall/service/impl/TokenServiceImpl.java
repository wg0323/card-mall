package com.cardmall.service.impl;

import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import com.cardmall.common.LoginResult;
import com.cardmall.common.TokenPair;
import com.cardmall.security.JwtTokenProvider;
import com.cardmall.security.LoginType;
import com.cardmall.security.LoginUser;
import com.cardmall.security.RefreshTokenPayload;
import com.cardmall.security.RefreshTokenStore;
import com.cardmall.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private static final String MSG_REFRESH_INVALID = "登录已失效，请重新登录";

    private final JwtTokenProvider jwtTokenProvider;

    private final RefreshTokenStore refreshTokenStore;

    @Value("${card-mall.jwt.access-expire-seconds:1800}")
    private long accessExpireSeconds;

    @Value("${card-mall.jwt.refresh-expire-seconds:43200}")
    private long refreshExpireSeconds;

    @Override
    public LoginResult issue(Long userId, String name, LoginType loginType, Integer role) {
        LoginUser user = new LoginUser(userId, loginType, role);
        return new LoginResult(jwtTokenProvider.createAccess(user), userId, name, role,
                issueRefreshToken(user));
    }

    /**
     * 轮换：旧的 refresh token 用掉即作废，同时下发新的一对，避免刷新令牌被反复使用。
     */
    @Override
    public TokenPair rotate(String refreshToken, LoginType expectedType) {
        RefreshTokenPayload payload = jwtTokenProvider.parseRefresh(refreshToken);
        if (payload.getUser().getLoginType() != expectedType) {
            throw new BizException(ErrorCode.APP_UNAUTHORIZED, MSG_REFRESH_INVALID);
        }
        if (!refreshTokenStore.consume(payload.getJti())) {
            throw new BizException(ErrorCode.APP_UNAUTHORIZED, MSG_REFRESH_INVALID);
        }
        LoginUser user = payload.getUser();
        return new TokenPair(jwtTokenProvider.createAccess(user), issueRefreshToken(user),
                accessExpireSeconds, refreshExpireSeconds);
    }

    /**
     * 退出登录只需撤销刷新令牌：access token 到期自然失效，服务端不维护会话。
     */
    @Override
    public void revoke(String refreshToken) {
        try {
            refreshTokenStore.remove(jwtTokenProvider.parseRefresh(refreshToken).getJti());
        } catch (RuntimeException e) {
            log.debug("revoke skipped: refresh token invalid");
        }
    }

    private String issueRefreshToken(LoginUser user) {
        String jti = UUID.randomUUID().toString().replace("-", "");
        String refreshToken = jwtTokenProvider.createRefresh(user, jti);
        refreshTokenStore.save(jti, user.getUserId());
        return refreshToken;
    }
}
