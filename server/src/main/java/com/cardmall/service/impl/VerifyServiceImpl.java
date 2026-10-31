package com.cardmall.service.impl;

import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import com.cardmall.entity.MemberCard;
import com.cardmall.entity.MemberInfo;
import com.cardmall.entity.VerifyRecord;
import com.cardmall.mapper.MemberInfoMapper;
import com.cardmall.mapper.VerifyRecordMapper;
import com.cardmall.service.MemberCardService;
import com.cardmall.service.VerifyAttemptService;
import com.cardmall.service.VerifyCodeService;
import com.cardmall.service.VerifyService;
import com.cardmall.service.VerifyTxService;
import com.cardmall.vo.VerifyPreviewVO;
import com.cardmall.vo.VerifyResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VerifyServiceImpl implements VerifyService {

    private final VerifyCodeService verifyCodeService;

    private final VerifyAttemptService verifyAttemptService;

    private final VerifyTxService verifyTxService;

    private final VerifyRecordMapper verifyRecordMapper;

    private final MemberCardService memberCardService;

    private final MemberInfoMapper memberInfoMapper;

    @Value("${card-mall.verify.max-times-per-verify:10}")
    private int maxTimes;

    /** 单店阶段门店 ID 直接取配置，与 StoreServiceImpl 同源；无需为一次闸门校验多查一次库 */
    @Value("${card-mall.store-id:1}")
    private Long storeId;

    /**
     * 店员扫码后的预览：只读，不扣次不删码。
     * 这是最容易用来撞库的入口（只看不扣），所以先过限流闸门。
     */
    @Override
    public VerifyPreviewVO preview(Long staffId, String token) {
        verifyAttemptService.checkNotLocked(storeId, staffId);
        Long cardId;
        try {
            cardId = verifyCodeService.peek(token);
        } catch (BizException e) {
            // 码根本不存在，是最典型的猜测特征；卡已用完/过期这类业务状态不计数
            verifyAttemptService.recordFailure(storeId, staffId);
            throw e;
        }
        verifyAttemptService.clear(staffId);
        MemberCard card = memberCardService.checkUsableById(cardId);
        MemberInfo member = memberInfoMapper.selectById(card.getMemberId());

        VerifyPreviewVO vo = new VerifyPreviewVO();
        vo.setCardId(card.getId());
        vo.setCardNo(card.getCardNo());
        vo.setCardName(card.getCardName());
        vo.setCardType(card.getCardType());
        vo.setRemainingTimes(card.getRemainingTimes());
        vo.setValidEnd(card.getValidEnd());
        vo.setMemberNickname(member == null ? "" : member.getNickname());
        vo.setMemberPhone(member == null ? "" : member.getPhone());
        vo.setMaxTimes(maxTimesFor(card));
        return vo;
    }

    /**
     * 顺序很关键：先查 requestId 幂等，再消费核销码。
     * 反过来的话，一次重放就会把有效码吃掉，顾客得重新开码。
     */
    @Override
    public VerifyResultVO confirm(Long staffId, String token, String requestId, Integer times, String remark) {
        verifyAttemptService.checkNotLocked(storeId, staffId);
        int deductTimes = times == null ? 1 : times;
        if (deductTimes < 1 || deductTimes > maxTimes) {
            throw new BizException(ErrorCode.VERIFY_TIMES_INVALID,
                    "单次核销次数须在 1 到 " + maxTimes + " 之间");
        }

        VerifyRecord existing = verifyRecordMapper.selectByRequestId(requestId);
        if (existing != null) {
            // 码还在且指向另一张卡：不是重试，是复用了 requestId，必须报错而不是静默成功
            Long codeCardId = verifyCodeService.peekOrNull(token);
            if (codeCardId != null && !codeCardId.equals(existing.getMemberCardId())) {
                throw new BizException(ErrorCode.VERIFY_CODE_INVALID, "核销码与本次请求不匹配，请重新扫码");
            }
            return VerifyResultVO.of(existing);
        }

        Long cardId = verifyCodeService.consume(token);
        if (cardId == null) {
            verifyAttemptService.recordFailure(storeId, staffId);
            throw new BizException(ErrorCode.VERIFY_CODE_INVALID, "核销码已失效，请让顾客重新出示");
        }
        verifyAttemptService.clear(staffId);

        try {
            return VerifyResultVO.of(verifyTxService.doConfirm(staffId, cardId, requestId, deductTimes, remark));
        } catch (DuplicateKeyException e) {
            // 同一 requestId 被并发抢先：本事务已回滚，回查赢家结果返回
            VerifyRecord winner = verifyRecordMapper.selectByRequestId(requestId);
            if (winner == null) {
                throw new BizException(ErrorCode.VERIFY_CODE_INVALID, "核销失败，请重新扫码");
            }
            return VerifyResultVO.of(winner);
        } catch (RuntimeException e) {
            // 扣次失败不能把顾客的码一起烧掉，否则报一次错就得重新开码
            verifyCodeService.restore(token, cardId);
            throw e;
        }
    }

    private int maxTimesFor(MemberCard card) {
        if (card.getCardType() == null || card.getCardType() != MemberCard.TYPE_COUNT) {
            return 1;
        }
        int remaining = card.getRemainingTimes() == null ? 0 : card.getRemainingTimes();
        return Math.min(remaining, maxTimes);
    }
}
