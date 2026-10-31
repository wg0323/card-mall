package com.cardmall.service;

import com.cardmall.entity.MemberCard;
import com.cardmall.vo.CodeResult;

public interface VerifyCodeService {

    /** 码里只放 cardId，不放卡号与余次，避免被枚举或伪造 */
    CodeResult issue(Long memberId, Long cardId);

    /** 原子取出并删除：取不到说明已用过或已过期 */
    Long consume(String token);

    /** 只读不删，码不存在时返回 null */
    Long peekOrNull(String token);

    /** 落库失败时把码放回去，避免一次失败就把顾客的码烧掉 */
    void restore(String token, Long cardId);

    /** 只读不删，用于店员确认前的预览 */
    Long peek(String token);

    long getTtlSeconds();

    /** 测试与排障用：卡 id → 码内容，不该出现在生产调用链里 */
    MemberCard loadCard(Long cardId);
}
