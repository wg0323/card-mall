package com.cardmall.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * 刷新令牌白名单：access token 是无状态 JWT，但 refresh token 必须可撤销，
 * 所以每个 jti 在 Redis 留一条记录，轮换时先删旧的再发新的。
 */
@Component
public class RefreshTokenStore {

    private static final String KEY_PREFIX = "card-mall:auth:refresh:";

    private final StringRedisTemplate redisTemplate;

    private final long refreshExpireSeconds;

    public RefreshTokenStore(StringRedisTemplate redisTemplate,
                             @Value("${card-mall.jwt.refresh-expire-seconds:43200}") long refreshExpireSeconds) {
        this.redisTemplate = redisTemplate;
        this.refreshExpireSeconds = refreshExpireSeconds;
    }

    public void save(String jti, Long userId) {
        redisTemplate.opsForValue().set(KEY_PREFIX + jti, String.valueOf(userId),
                Duration.ofSeconds(refreshExpireSeconds));
    }

    /**
     * 取出并删除：同一 refresh token 只能换一次令牌，重放会拿不到记录。
     */
    public boolean consume(String jti) {
        return redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + jti) != null;
    }

    public void remove(String jti) {
        redisTemplate.delete(KEY_PREFIX + jti);
    }
}
