package com.cardmall.converter;

import com.cardmall.entity.PayTransaction;
import com.cardmall.vo.PayTransactionVO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对象转换统一放这里，Controller/Service 里不做字段级拷贝。
 */
public final class PayTransactionConverter {

    private PayTransactionConverter() {
    }

    public static PayTransactionVO toVO(PayTransaction transaction) {
        if (transaction == null) {
            return null;
        }
        PayTransactionVO vo = new PayTransactionVO();
        vo.setId(transaction.getId());
        vo.setOrderNo(transaction.getOrderNo());
        vo.setChannel(transaction.getChannel());
        vo.setTransactionId(transaction.getTransactionId());
        vo.setAmount(transaction.getAmount());
        vo.setStatus(transaction.getStatus());
        vo.setFinishTime(transaction.getFinishTime());
        vo.setCreateTime(transaction.getCreateTime());
        return vo;
    }

    public static List<PayTransactionVO> toVOList(List<PayTransaction> transactions) {
        if (transactions == null) {
            return new ArrayList<PayTransactionVO>();
        }
        return transactions.stream().map(PayTransactionConverter::toVO).collect(Collectors.toList());
    }
}
