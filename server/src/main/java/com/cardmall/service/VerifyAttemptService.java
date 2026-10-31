package com.cardmall.service;

/**
 * 核销码尝试次数闸门：防止拿店员账号穷举核销码。
 * 两层：单个店员、整个门店（多账号轮流试也会被门店这层拦住）。
 */
public interface VerifyAttemptService {

    /** 门店或店员任一被锁定就抛业务异常；都没锁则什么都不做 */
    void checkNotLocked(Long storeId, Long staffId);

    /** 记一次"核销码无效"，任一层达到阈值即转为锁定 */
    void recordFailure(Long storeId, Long staffId);

    /**
     * 核销成功后清掉店员计数。
     * 门店计数不清：否则一次成功就抹掉同门店其它账号的尝试痕迹，门店闸门形同虚设；
     * 它只靠时间窗口自然衰减。
     */
    void clear(Long staffId);
}
