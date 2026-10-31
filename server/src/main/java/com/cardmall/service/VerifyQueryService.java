package com.cardmall.service;

import com.cardmall.common.PageResult;
import com.cardmall.entity.VerifyRecord;
import com.cardmall.vo.VerifyRecordVO;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 核销记录查询。店员端与后台共用，避免两套 SQL。
 */
public interface VerifyQueryService {

    long todayCount(Long storeId);

    PageResult<VerifyRecordVO> page(Long storeId, LocalDate startDate, LocalDate endDate,
                                    Integer status, int pageNum, int pageSize);

    VerifyRecordVO getDetail(Long id);

    /** 卡详情页的最近使用记录 */
    List<VerifyRecord> listRecentByCard(Long cardId, int limit);

    /** 按会员批量汇总核销次数，会员列表统计用 */
    Map<Long, Integer> sumVerifyTimesByMembers(Collection<Long> memberIds);

    /** 会员详情页的核销记录，最近的在前 */
    List<VerifyRecordVO> listByMember(Long memberId, int limit);
}
