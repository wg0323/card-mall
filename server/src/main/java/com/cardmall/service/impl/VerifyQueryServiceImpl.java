package com.cardmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cardmall.common.PageResult;
import com.cardmall.entity.MemberCard;
import com.cardmall.entity.MemberInfo;
import com.cardmall.entity.Staff;
import com.cardmall.entity.VerifyRecord;
import com.cardmall.mapper.MemberCardMapper;
import com.cardmall.mapper.MemberInfoMapper;
import com.cardmall.mapper.StaffMapper;
import com.cardmall.mapper.VerifyRecordMapper;
import com.cardmall.service.VerifyQueryService;
import com.cardmall.vo.VerifyRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 核销记录查询。店员端与后台共用，避免两套 SQL。
 */
@Service
@RequiredArgsConstructor
public class VerifyQueryServiceImpl implements VerifyQueryService {

    private final VerifyRecordMapper verifyRecordMapper;

    private final MemberCardMapper memberCardMapper;

    private final MemberInfoMapper memberInfoMapper;

    private final StaffMapper staffMapper;

    @Override
    public long todayCount(Long storeId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        return verifyRecordMapper.selectCount(new QueryWrapper<VerifyRecord>()
                .eq("store_id", storeId)
                .eq("status", VerifyRecord.STATUS_NORMAL)
                .ge("verify_time", start));
    }

    @Override
    public PageResult<VerifyRecordVO> page(Long storeId, LocalDate startDate, LocalDate endDate,
                                           Integer status, int pageNum, int pageSize) {
        QueryWrapper<VerifyRecord> qw = new QueryWrapper<VerifyRecord>().orderByDesc("verify_time");
        if (storeId != null) {
            qw.eq("store_id", storeId);
        }
        if (startDate != null) {
            qw.ge("verify_time", startDate.atStartOfDay());
        }
        if (endDate != null) {
            qw.le("verify_time", endDate.atTime(23, 59, 59));
        }
        if (status != null) {
            qw.eq("status", status);
        }
        Page<VerifyRecord> result = verifyRecordMapper.selectPage(
                new Page<VerifyRecord>(pageNum, pageSize), qw);
        return PageResult.of(toVOList(result.getRecords()), result.getTotal());
    }

    @Override
    public VerifyRecordVO getDetail(Long id) {
        VerifyRecord record = verifyRecordMapper.selectById(id);
        if (record == null) {
            return null;
        }
        List<VerifyRecord> one = new ArrayList<VerifyRecord>();
        one.add(record);
        List<VerifyRecordVO> vos = toVOList(one);
        return vos.isEmpty() ? null : vos.get(0);
    }

    @Override
    public List<VerifyRecord> listRecentByCard(Long cardId, int limit) {
        return verifyRecordMapper.selectRecentByCard(cardId, limit);
    }

    @Override
    public Map<Long, Integer> sumVerifyTimesByMembers(Collection<Long> memberIds) {
        Map<Long, Integer> sums = new HashMap<Long, Integer>();
        if (memberIds == null || memberIds.isEmpty()) {
            return sums;
        }
        List<VerifyRecord> records = verifyRecordMapper.selectList(new QueryWrapper<VerifyRecord>()
                .select("member_id", "verify_times")
                .eq("status", VerifyRecord.STATUS_NORMAL)
                .in("member_id", memberIds));
        for (VerifyRecord record : records) {
            Integer times = record.getVerifyTimes();
            sums.merge(record.getMemberId(), times == null ? 0 : times, Integer::sum);
        }
        return sums;
    }

    @Override
    public List<VerifyRecordVO> listByMember(Long memberId, int limit) {
        if (memberId == null || limit <= 0) {
            return new ArrayList<VerifyRecordVO>();
        }
        List<VerifyRecord> records = verifyRecordMapper.selectList(new QueryWrapper<VerifyRecord>()
                .eq("member_id", memberId)
                .orderByDesc("verify_time")
                .last("limit " + limit));
        return toVOList(records);
    }

    /**
     * 记录里只有各种 id，卡名/会员昵称/员工姓名要在这里补齐。
     * 一次批量查，避免逐条查库。
     */
    private List<VerifyRecordVO> toVOList(List<VerifyRecord> records) {
        List<VerifyRecordVO> list = new ArrayList<VerifyRecordVO>();
        if (records == null || records.isEmpty()) {
            return list;
        }

        Set<Long> cardIds = new HashSet<Long>();
        Set<Long> memberIds = new HashSet<Long>();
        Set<Long> staffIds = new HashSet<Long>();
        for (VerifyRecord r : records) {
            cardIds.add(r.getMemberCardId());
            memberIds.add(r.getMemberId());
            staffIds.add(r.getStaffId());
        }

        Map<Long, MemberCard> cardMap = new HashMap<Long, MemberCard>();
        for (MemberCard card : memberCardMapper.selectBatchIds(cardIds)) {
            cardMap.put(card.getId(), card);
        }
        Map<Long, MemberInfo> memberMap = new HashMap<Long, MemberInfo>();
        for (MemberInfo member : memberInfoMapper.selectBatchIds(memberIds)) {
            memberMap.put(member.getId(), member);
        }
        Map<Long, Staff> staffMap = new HashMap<Long, Staff>();
        for (Staff staff : staffMapper.selectBatchIds(staffIds)) {
            staffMap.put(staff.getId(), staff);
        }

        for (VerifyRecord r : records) {
            VerifyRecordVO vo = new VerifyRecordVO();
            vo.setId(r.getId());
            vo.setRecordNo(r.getRecordNo());
            vo.setCardId(r.getMemberCardId());
            vo.setMemberId(r.getMemberId());
            vo.setStaffId(r.getStaffId());
            vo.setVerifyTimes(r.getVerifyTimes());
            vo.setRemainingBefore(r.getRemainingBefore());
            vo.setRemainingAfter(r.getRemainingAfter());
            vo.setStatus(r.getStatus());
            vo.setRemark(r.getRemark());
            vo.setVerifyTime(r.getVerifyTime());

            MemberCard card = cardMap.get(r.getMemberCardId());
            if (card != null) {
                vo.setCardName(card.getCardName());
            }
            MemberInfo member = memberMap.get(r.getMemberId());
            if (member != null) {
                vo.setMemberNickname(member.getNickname());
                vo.setMemberPhone(member.getPhone());
            }
            Staff staff = staffMap.get(r.getStaffId());
            if (staff != null) {
                vo.setStaffName(staff.getName());
            }
            list.add(vo);
        }
        return list;
    }
}
