package com.cardmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cardmall.common.PageResult;
import com.cardmall.entity.CardOrder;
import com.cardmall.entity.MemberCard;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface MemberCardService extends IService<MemberCard> {

    /** 支付成功后发卡，卡名/次数/类型做快照 */
    void issueCard(CardOrder order);

    PageResult<MemberCard> pageByMember(Long memberId, Integer status, int pageNum, int pageSize);

    MemberCard getCardById(Long cardId);

    List<MemberCard> listByMember(Long memberId);

    /** 按会员批量数持卡数，会员列表统计用 */
    Map<Long, Integer> countByMembers(Collection<Long> memberIds);

    /** 只校验归属，已用完/已过期的卡也能回看记录 */
    MemberCard checkOwner(Long memberId, Long cardId);

    /** 开码与核销前的统一校验：归属、状态、有效期 */
    MemberCard checkUsable(Long memberId, Long cardId);

    /** 核销时按卡 id 校验（扫码方是店员，没有 memberId 上下文） */
    MemberCard checkUsableById(Long cardId);
}
