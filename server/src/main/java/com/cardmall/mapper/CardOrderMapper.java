package com.cardmall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cardmall.entity.CardOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CardOrderMapper extends BaseMapper<CardOrder> {

    /**
     * 找一笔可评价的已支付订单：同一卡产品只取最早的一笔，且该订单还没有评价。
     * 用 not exists 一次查完，避免先查订单再逐条查评论。
     */
    @Select("select o.order_no from card_order o "
            + "where o.member_id = #{memberId} and o.product_id = #{productId} and o.pay_status = 1 "
            + "and not exists (select 1 from card_comment c where c.order_no = o.order_no) "
            + "order by o.create_time asc limit 1")
    String selectFirstCommentableOrderNo(@Param("memberId") Long memberId, @Param("productId") Long productId);

    /** 指定订单是否可评论：从订单详情进入时用，避免评价挂到同产品的另一笔订单上 */
    @Select("select o.order_no from card_order o "
            + "where o.member_id = #{memberId} and o.product_id = #{productId} and o.pay_status = 1 "
            + "and o.order_no = #{orderNo} "
            + "and not exists (select 1 from card_comment c where c.order_no = o.order_no) limit 1")
    String selectCommentableOrderNo(@Param("memberId") Long memberId, @Param("productId") Long productId,
                                    @Param("orderNo") String orderNo);
}
