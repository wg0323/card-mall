package com.cardmall.service;

import com.cardmall.entity.VerifyRecord;

/**
 * 核销落库。必须单独成 Bean：同类自调用会让 @Transactional 静默失效。
 */
public interface VerifyTxService {

    VerifyRecord doConfirm(Long staffId, Long cardId, String requestId, int times, String remark);
}
