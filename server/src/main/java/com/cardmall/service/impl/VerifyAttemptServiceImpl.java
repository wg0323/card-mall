package com.cardmall.service.impl;

import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import com.cardmall.service.VerifyAttemptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 失败计数与锁定，计数与阈值都放 Redis。
 * 日志统一带 [核销告警] 前缀，日志采集侧按这个关键字配告警规则即可。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyAttemptServiceImpl implements VerifyAttemptService {

    private static final String FAIL_KEY_PREFIX = "verify:fail:";

    private static final String LOCK_KEY_PREFIX = "verify:lock:";

    private static final String STORE_FAIL_KEY_PREFIX = "verify:fail:store:";

    private static final String STORE_LOCK_KEY_PREFIX = "verify:lock:store:";

    private final StringRedisTemplate redisTemplate;

    @Value("${card-mall.verify.max-fail-times:5}")
    private int maxFailTimes;

    @Value("${card-mall.verify.fail-window-seconds:60}")
    private long failWindowSeconds;

    @Value("${card-mall.verify.lock-seconds:60}")
    private long lockSeconds;

    /** 门店闸门阈值更高：一个门店多名店员，正常的打错次数会叠加 */
    @Value("${card-mall.verify.max-fail-times-per-store:30}")
    private int maxFailTimesPerStore;

    @Value("${card-mall.verify.store-lock-seconds:120}")
    private long storeLockSeconds;

    @Override
    public void checkNotLocked(Long storeId, Long staffId) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(STORE_LOCK_KEY_PREFIX + storeId))) {
            throw new BizException(ErrorCode.VERIFY_CODE_INVALID, "门店核销码尝试过于频繁，请稍后再试");
        }
        if (Boolean.TRUE.equals(redisTemplate.hasKey(LOCK_KEY_PREFIX + staffId))) {
            throw new BizException(ErrorCode.VERIFY_CODE_INVALID, "核销码尝试过于频繁，请稍后再试");
        }
    }

    @Override
    public void recordFailure(Long storeId, Long staffId) {
        recordStaffFailure(staffId);
        recordStoreFailure(storeId);
    }

    @Override
    public void clear(Long staffId) {
        redisTemplate.delete(FAIL_KEY_PREFIX + staffId);
    }

    private void recordStaffFailure(Long staffId) {
        String failKey = FAIL_KEY_PREFIX + staffId;
        Long failures = increaseWithinWindow(failKey);
        if (failures == null || failures < maxFailTimes) {
            return;
        }
        redisTemplate.opsForValue().set(LOCK_KEY_PREFIX + staffId, "1", Duration.ofSeconds(lockSeconds));
        redisTemplate.delete(failKey);
        log.warn("[核销告警] 店员 {} 连续 {} 次核销码无效，已锁定 {} 秒", staffId, failures, lockSeconds);
    }

    private void recordStoreFailure(Long storeId) {
        String storeFailKey = STORE_FAIL_KEY_PREFIX + storeId;
        Long failures = increaseWithinWindow(storeFailKey);
        if (failures == null || failures < maxFailTimesPerStore) {
            return;
        }
        redisTemplate.opsForValue().set(STORE_LOCK_KEY_PREFIX + storeId, "1", Duration.ofSeconds(storeLockSeconds));
        redisTemplate.delete(storeFailKey);
        log.error("[核销告警] 门店 {} 在 {} 秒内累计 {} 次核销码无效，已锁定 {} 秒，疑似多账号轮流试码",
                storeId, failWindowSeconds, failures, storeLockSeconds);
    }

    /** 计数并刷新窗口：统计的是"最近 failWindowSeconds 秒内的失败次数" */
    private Long increaseWithinWindow(String key) {
        Long count = redisTemplate.opsForValue().increment(key);
        redisTemplate.expire(key, Duration.ofSeconds(failWindowSeconds));
        return count;
    }
}
