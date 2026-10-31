package com.cardmall.converter;

import com.cardmall.entity.MemberCard;
import com.cardmall.vo.MemberCardVO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对象转换统一放这里，Controller/Service 里不做字段级拷贝。
 */
public final class MemberCardConverter {

    private MemberCardConverter() {
    }

    public static MemberCardVO toVO(MemberCard card) {
        if (card == null) {
            return null;
        }
        MemberCardVO vo = new MemberCardVO();
        vo.setId(card.getId());
        vo.setCardNo(card.getCardNo());
        vo.setMemberId(card.getMemberId());
        vo.setProductId(card.getProductId());
        vo.setOrderNo(card.getOrderNo());
        vo.setCardName(card.getCardName());
        vo.setCardType(card.getCardType());
        vo.setTotalTimes(card.getTotalTimes());
        vo.setRemainingTimes(card.getRemainingTimes());
        vo.setValidStart(card.getValidStart());
        vo.setValidEnd(card.getValidEnd());
        vo.setStatus(card.getStatus());
        vo.setCreateTime(card.getCreateTime());
        vo.setUpdateTime(card.getUpdateTime());
        return vo;
    }

    public static List<MemberCardVO> toVOList(List<MemberCard> cards) {
        if (cards == null) {
            return new ArrayList<MemberCardVO>();
        }
        return cards.stream().map(MemberCardConverter::toVO).collect(Collectors.toList());
    }
}
