package com.cardmall.converter;

import com.cardmall.entity.CardOrder;
import com.cardmall.entity.MemberInfo;
import com.cardmall.vo.CardOrderVO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对象转换统一放这里，Controller/Service 里不做字段级拷贝。
 */
public final class CardOrderConverter {

    private CardOrderConverter() {
    }

    public static CardOrderVO toVO(CardOrder order) {
        if (order == null) {
            return null;
        }
        CardOrderVO vo = new CardOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setStoreId(order.getStoreId());
        vo.setMemberId(order.getMemberId());
        vo.setProductId(order.getProductId());
        vo.setProductName(order.getProductName());
        vo.setAmount(order.getAmount());
        vo.setPayChannel(order.getPayChannel());
        vo.setPayStatus(order.getPayStatus());
        vo.setPaidTime(order.getPaidTime());
        vo.setCloseTime(order.getCloseTime());
        vo.setCreateTime(order.getCreateTime());
        vo.setUpdateTime(order.getUpdateTime());
        return vo;
    }

    /** 后台列表用：顺带把会员昵称与手机号填上，避免前端只看到会员ID */
    public static CardOrderVO toVO(CardOrder order, MemberInfo member) {
        CardOrderVO vo = toVO(order);
        if (vo != null && member != null) {
            vo.setMemberNickname(member.getNickname());
            vo.setMemberPhone(member.getPhone());
        }
        return vo;
    }

    public static List<CardOrderVO> toVOList(List<CardOrder> orders) {
        if (orders == null) {
            return new ArrayList<CardOrderVO>();
        }
        return orders.stream().map(CardOrderConverter::toVO).collect(Collectors.toList());
    }
}
