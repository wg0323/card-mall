package com.cardmall.security;

import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 双令牌：access 短有效期且无状态，refresh 长有效期并带 jti 存 Redis 白名单。
 * 两种令牌靠 kind 区分，refresh 令牌不能直接当访问凭证用。
 */
@Component
public class JwtTokenProvider {

    private static final String CLAIM_TYPE = "type";

    private static final String CLAIM_ROLE = "role";

    private static final String CLAIM_KIND = "kind";

    private static final String KIND_ACCESS = "access";

    private static final String KIND_REFRESH = "refresh";

    private static final String MSG_INVALID = "登录已失效，请重新登录";

    @Value("${card-mall.jwt.secret}")
    private String secret;

    @Value("${card-mall.jwt.access-expire-seconds:1800}")
    private long accessExpireSeconds;

    @Value("${card-mall.jwt.refresh-expire-seconds:43200}")
    private long refreshExpireSeconds;

    private SecretKey key;

    @PostConstruct
    public void init() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalStateException("card-mall.jwt.secret 至少需要 32 字节");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccess(LoginUser user) {
        return build(user, KIND_ACCESS, accessExpireSeconds, null);
    }

    public String createRefresh(LoginUser user, String jti) {
        return build(user, KIND_REFRESH, refreshExpireSeconds, jti);
    }

    /**
     * 访问令牌解析：过期异常原样抛出，过滤器据此回 TOKEN_EXPIRED 让前端去刷新；
     * 签名错误、结构损坏、类型不符则统一当未登录。
     */
    public LoginUser parseAccess(String token) {
        Claims claims = parseClaims(token);
        requireKind(claims, KIND_ACCESS);
        return toLoginUser(claims);
    }

    /**
     * 刷新令牌解析：过期就是彻底失效，前端只能重新登录。
     */
    public RefreshTokenPayload parseRefresh(String token) {
        Claims claims;
        try {
            claims = parseClaims(token);
        } catch (ExpiredJwtException e) {
            throw new BizException(ErrorCode.APP_UNAUTHORIZED, MSG_INVALID);
        }
        requireKind(claims, KIND_REFRESH);
        return new RefreshTokenPayload(claims.getId(), toLoginUser(claims));
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build()
                    .parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(ErrorCode.APP_UNAUTHORIZED, MSG_INVALID);
        }
    }

    private void requireKind(Claims claims, String expected) {
        if (!expected.equals(claims.get(CLAIM_KIND, String.class))) {
            throw new BizException(ErrorCode.APP_UNAUTHORIZED, MSG_INVALID);
        }
    }

    private LoginUser toLoginUser(Claims claims) {
        LoginType type = LoginType.valueOf(claims.get(CLAIM_TYPE, String.class));
        Integer role = claims.get(CLAIM_ROLE, Integer.class);
        return new LoginUser(Long.valueOf(claims.getSubject()), type, role);
    }

    private String build(LoginUser user, String kind, long expireSeconds, String jti) {
        Date now = new Date();
        JwtBuilder builder = Jwts.builder()
                .setSubject(String.valueOf(user.getUserId()))
                .claim(CLAIM_TYPE, user.getLoginType().name())
                .claim(CLAIM_ROLE, user.getRole())
                .claim(CLAIM_KIND, kind)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expireSeconds * 1000L))
                .signWith(key, SignatureAlgorithm.HS256);
        if (jti != null) {
            builder.setId(jti);
        }
        return builder.compact();
    }
}
