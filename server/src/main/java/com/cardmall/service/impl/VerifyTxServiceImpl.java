package com.cardmall.service.impl;

import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import com.cardmall.entity.MemberCard;
import com.cardmall.entity.VerifyRecord;
import com.cardmall.mapper.MemberCardMapper;
import com.cardmall.mapper.VerifyRecordMapper;
import com.cardmall.service.MemberCardService;
import com.cardmall.service.StoreService;
import com.cardmall.service.VerifyTxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class VerifyTxServiceImpl implements VerifyTxService {

    private static final DateTimeFormatter RECORD_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final MemberCardMapper memberCardMapper;

    private final MemberCardService memberCardService;

    private final VerifyRecordMapper verifyRecordMapper;

    private final StoreService storeService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public VerifyRecord doConfirm(Long staffId, Long cardId, String requestId, int times, String remark) {
        MemberCard card = memberCardService.checkUsableById(cardId);

        Integer before = null;
        Integer after = null;
        if (card.getCardType() != null && card.getCardType() == MemberCard.TYPE_COUNT) {
            int affected = memberCardMapper.deduct(cardId, times);
            if (affected == 0) {
                throw new BizException(ErrorCode.CARD_TIMES_NOT_ENOUGH, "剩余次数不足");
            }
            // 无条件调用：只有剩余归 0 时 WHERE 才命中，不靠 Java 侧算出的值判断是否置尾
            memberCardMapper.markUsedUp(cardId);
            // 并发下 before 可能是旧快照，扣完重读一次才能拿到真实余次
            MemberCard latest = memberCardMapper.selectById(cardId);
            after = latest.getRemainingTimes();
            before = after + times;
        }

        VerifyRecord record = new VerifyRecord();
        record.setRecordNo(generateRecordNo());
        record.setRequestId(requestId);
        record.setMemberCardId(cardId);
        record.setMemberId(card.getMemberId());
        record.setStaffId(staffId);
        record.setStoreId(storeService.getMainStore().getId());
        record.setVerifyTimes(before == null ? 0 : times);
        record.setRemainingBefore(before);
        record.setRemainingAfter(after);
        record.setStatus(VerifyRecord.STATUS_NORMAL);
        record.setRemark(remark == null ? "" : remark);
        record.setVerifyTime(LocalDateTime.now());
        // uk_request_id 冲突会抛异常，事务回滚，上面的扣次一起撤销
        verifyRecordMapper.insert(record);
        return record;
    }

    private String generateRecordNo() {
        return LocalDateTime.now().format(RECORD_NO_FORMATTER)
                + ThreadLocalRandom.current().nextInt(1000, 10000);
    }
}
