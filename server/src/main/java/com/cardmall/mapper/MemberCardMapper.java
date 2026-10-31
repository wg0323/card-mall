package com.cardmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cardmall.entity.MemberCard;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface MemberCardMapper extends BaseMapper<MemberCard> {

    /**
     * 扣次唯一正确姿势：让数据库做 CAS，不在 Java 里先查再减。
     * 条件里的 remaining_times >= times 保证一次扣多次时要么整笔成功、要么一笔不扣。
     */
    @Update("update member_card set remaining_times = remaining_times - #{times}, update_time = now() "
            + "where id = #{cardId} and status = 1 and remaining_times >= #{times}")
    int deduct(@Param("cardId") Long cardId, @Param("times") int times);

    @Update("update member_card set status = 2, update_time = now() "
            + "where id = #{cardId} and status = 1 and remaining_times = 0")
    int markUsedUp(@Param("cardId") Long cardId);
}
