package com.cardmall.converter;

import com.cardmall.entity.VerifyRecord;
import com.cardmall.vo.CardRecordVO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对象转换统一放这里，Controller/Service 里不做字段级拷贝。
 */
public final class CardRecordConverter {

    private CardRecordConverter() {
    }

    public static CardRecordVO toVO(VerifyRecord record) {
        if (record == null) {
            return null;
        }
        CardRecordVO vo = new CardRecordVO();
        vo.setId(record.getId());
        vo.setVerifyTimes(record.getVerifyTimes());
        vo.setRemainingBefore(record.getRemainingBefore());
        vo.setRemainingAfter(record.getRemainingAfter());
        vo.setRemark(record.getRemark());
        vo.setStatus(record.getStatus());
        vo.setVerifyTime(record.getVerifyTime());
        return vo;
    }

    public static List<CardRecordVO> toVOList(List<VerifyRecord> records) {
        if (records == null) {
            return new ArrayList<CardRecordVO>();
        }
        return records.stream().map(CardRecordConverter::toVO).collect(Collectors.toList());
    }
}
