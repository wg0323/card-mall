package com.cardmall.service.impl;

import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import com.cardmall.entity.MemberCard;
import com.cardmall.service.MemberCardService;
import com.cardmall.service.VerifyCodeService;
import com.cardmall.vo.CodeResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class VerifyCodeServiceImpl implements VerifyCodeService {

    private static final String KEY_PREFIX = "verify:code:";

    /**
     * 核销码位数。8 位数字 = 一亿个取值：店员能口述、能手输，
     * 而 60 秒窗口内想穷举出一张别人的活码不现实（6 位只有百万级，脚本可枚举）。
     */
    private static final int CODE_LENGTH = 8;

    /** 十位进制基数，用于逐位生成数字 */
    private static final int DIGIT_BASE = 10;

    /** 撞上活码就换一个，8 位下同时存在的码极少，基本一次就中 */
    private static final int MAX_ISSUE_ATTEMPTS = 5;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final StringRedisTemplate redisTemplate;

    private final MemberCardService memberCardService;

    @Value("${card-mall.verify.code-ttl-seconds:60}")
    private long ttlSeconds;

    /**
     * 码里只放 cardId，不放卡号与余次，避免被枚举或伪造。
     * 唯一性靠 Redis 的 SETNX：撞上活码就重新生成，不依赖随机数本身不重复。
     */
    @Override
    public CodeResult issue(Long memberId, Long cardId) {
        memberCardService.checkUsable(memberId, cardId);
        Duration ttl = Duration.ofSeconds(ttlSeconds);
        for (int attempt = 0; attempt < MAX_ISSUE_ATTEMPTS; attempt++) {
            String token = randomCode();
            Boolean issued = redisTemplate.opsForValue()
                    .setIfAbsent(KEY_PREFIX + token, String.valueOf(cardId), ttl);
            if (Boolean.TRUE.equals(issued)) {
                return new CodeResult(token, ttlSeconds);
            }
        }
        throw new BizException(ErrorCode.VERIFY_CODE_INVALID, "核销码生成失败，请重试");
    }

    /** 首位取 1-9，避免手输时把前导 0 丢掉；其余位取 0-9 */
    private String randomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        code.append((char) ('0' + 1 + RANDOM.nextInt(DIGIT_BASE - 1)));
        for (int index = 1; index < CODE_LENGTH; index++) {
            code.append((char) ('0' + RANDOM.nextInt(DIGIT_BASE)));
        }
        return code.toString();
    }

    /**
     * 原子取出并删除：取不到说明已用过或已过期。需要 Redis 6.2+ 与 Spring Data Redis 2.6+。
     */
    @Override
    public Long consume(String token) {
        String value = redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + token);
        return value == null ? null : Long.valueOf(value);
    }

    /**
     * 只读不删，码不存在时返回 null（用于幂等短路时判断"是不是同一张卡"）。
     */
    @Override
    public Long peekOrNull(String token) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + token);
        return value == null ? null : Long.valueOf(value);
    }

    /**
     * 落库失败时把码放回去，避免"一次失败就把顾客的码烧掉"。
     * TTL 按完整有效期重算，即最多多活一个码周期，比让顾客重开码便宜。
     */
    @Override
    public void restore(String token, Long cardId) {
        redisTemplate.opsForValue().set(KEY_PREFIX + token, String.valueOf(cardId),
                Duration.ofSeconds(ttlSeconds));
    }

    /**
     * 只读不删，用于店员确认前的预览。
     */
    @Override
    public Long peek(String token) {
        String value = redisTemplate.opsForValue().get(KEY_PREFIX + token);
        if (value == null) {
            throw new BizException(ErrorCode.VERIFY_CODE_INVALID, "核销码已失效，请让顾客重新出示");
        }
        return Long.valueOf(value);
    }

    @Override
    public long getTtlSeconds() {
        return ttlSeconds;
    }

    /**
     * 测试与排障用：卡 id → 码内容，不该出现在生产调用链里。
     */
    @Override
    public MemberCard loadCard(Long cardId) {
        return memberCardService.checkUsableById(cardId);
    }
}
