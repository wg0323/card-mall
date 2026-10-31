package com.cardmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import com.cardmall.common.PageResult;
import com.cardmall.entity.CardOrder;
import com.cardmall.entity.CardProduct;
import com.cardmall.entity.MemberCard;
import com.cardmall.mapper.MemberCardMapper;
import com.cardmall.service.CardProductService;
import com.cardmall.service.MemberCardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemberCardServiceImpl extends ServiceImpl<MemberCardMapper, MemberCard> implements MemberCardService {

    private final CardProductService cardProductService;

    /**
     * 支付成功后发卡。卡名、次数、类型都做快照，后续改产品不影响已售出的卡。
     */
    @Override
    public void issueCard(CardOrder order) {
        CardProduct product = cardProductService.getProductById(order.getProductId());
        if (product == null) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND, "卡产品不存在");
        }
        boolean countCard = product.getCardType() != null
                && product.getCardType() == CardProduct.TYPE_COUNT;

        MemberCard card = new MemberCard();
        card.setCardNo("C" + order.getOrderNo());
        card.setMemberId(order.getMemberId());
        card.setProductId(product.getId());
        card.setOrderNo(order.getOrderNo());
        card.setCardName(product.getName());
        card.setCardType(product.getCardType());
        card.setTotalTimes(countCard ? product.getTotalTimes() : 0);
        card.setRemainingTimes(countCard ? product.getTotalTimes() : 0);
        card.setValidStart(LocalDateTime.now());
        card.setValidEnd(LocalDateTime.now().plusDays(product.getValidDays()));
        card.setStatus(MemberCard.STATUS_NORMAL);
        baseMapper.insert(card);
    }

    @Override
    public PageResult<MemberCard> pageByMember(Long memberId, Integer status, int pageNum, int pageSize) {
        QueryWrapper<MemberCard> qw = new QueryWrapper<MemberCard>()
                .eq("member_id", memberId)
                .orderByDesc("id");
        if (status != null) {
            qw.eq("status", status);
        }
        Page<MemberCard> result = baseMapper.selectPage(new Page<MemberCard>(pageNum, pageSize), qw);
        return PageResult.of(result.getRecords(), result.getTotal());
    }

    @Override
    public MemberCard getCardById(Long cardId) {
        return baseMapper.selectById(cardId);
    }

    @Override
    public List<MemberCard> listByMember(Long memberId) {
        return baseMapper.selectList(new QueryWrapper<MemberCard>()
                .eq("member_id", memberId)
                .orderByDesc("id"));
    }

    @Override
    public Map<Long, Integer> countByMembers(Collection<Long> memberIds) {
        Map<Long, Integer> counts = new HashMap<Long, Integer>();
        if (memberIds == null || memberIds.isEmpty()) {
            return counts;
        }
        List<MemberCard> cards = baseMapper.selectList(new QueryWrapper<MemberCard>()
                .select("member_id")
                .in("member_id", memberIds));
        for (MemberCard card : cards) {
            counts.merge(card.getMemberId(), 1, Integer::sum);
        }
        return counts;
    }

    /**
     * 只校验归属，不校验可用性。卡详情/历史记录对已用完、已过期的卡仍要看得到。
     */
    @Override
    public MemberCard checkOwner(Long memberId, Long cardId) {
        MemberCard card = baseMapper.selectById(cardId);
        if (card == null || !card.getMemberId().equals(memberId)) {
            throw new BizException(ErrorCode.CARD_NOT_USABLE, "卡不存在");
        }
        return card;
    }

    /**
     * 开码与核销前的统一校验：归属、状态、有效期。
     */
    @Override
    public MemberCard checkUsable(Long memberId, Long cardId) {
        MemberCard card = baseMapper.selectById(cardId);
        if (card == null || !card.getMemberId().equals(memberId)) {
            throw new BizException(ErrorCode.CARD_NOT_USABLE, "卡不存在");
        }
        if (card.getStatus() == null || card.getStatus() != MemberCard.STATUS_NORMAL) {
            throw new BizException(ErrorCode.CARD_NOT_USABLE, statusText(card.getStatus()));
        }
        if (card.getValidEnd().isBefore(LocalDateTime.now())) {
            throw new BizException(ErrorCode.CARD_NOT_USABLE, "该卡已过期");
        }
        return card;
    }

    /**
     * 核销时按卡 id 校验（此时没有 memberId 上下文，扫码方是店员）。
     */
    @Override
    public MemberCard checkUsableById(Long cardId) {
        MemberCard card = baseMapper.selectById(cardId);
        if (card == null) {
            throw new BizException(ErrorCode.VERIFY_CODE_INVALID, "核销码已失效");
        }
        if (card.getStatus() == null || card.getStatus() != MemberCard.STATUS_NORMAL) {
            throw new BizException(ErrorCode.CARD_NOT_USABLE, statusText(card.getStatus()));
        }
        if (card.getValidEnd().isBefore(LocalDateTime.now())) {
            throw new BizException(ErrorCode.CARD_NOT_USABLE, "该卡已过期");
        }
        return card;
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "该卡不可用";
        }
        switch (status) {
            case MemberCard.STATUS_USED_UP:
                return "该卡已用完";
            case MemberCard.STATUS_EXPIRED:
                return "该卡已过期";
            case MemberCard.STATUS_FROZEN:
                return "该卡已冻结";
            default:
                return "该卡不可用";
        }
    }
}
