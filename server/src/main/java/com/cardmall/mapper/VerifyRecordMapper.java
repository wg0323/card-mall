package com.cardmall.mapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cardmall.entity.VerifyRecord;

import java.util.List;

public interface VerifyRecordMapper extends BaseMapper<VerifyRecord> {

    /**
     * 用 selectOne 而不是 select *：字段变更时由 MyBatis-Plus 显式列映射兜住。
     */
    default VerifyRecord selectByRequestId(String requestId) {
        return selectOne(new QueryWrapper<VerifyRecord>().eq("request_id", requestId));
    }

    default List<VerifyRecord> selectRecentByCard(Long cardId, int limit) {
        return selectList(new QueryWrapper<VerifyRecord>()
                .eq("member_card_id", cardId)
                .orderByDesc("verify_time")
                .last("limit " + limit));
    }
}
