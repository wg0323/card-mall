package com.cardmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cardmall.common.PageResult;
import com.cardmall.dto.CommentCreateRequest;
import com.cardmall.entity.CardComment;
import com.cardmall.vo.AdminCommentVO;
import com.cardmall.vo.CommentVO;
import com.cardmall.vo.MyCommentVO;

public interface CommentService extends IService<CardComment> {

    /** 卡详情评论列表，只返回显示中的评论 */
    PageResult<CommentVO> pageByProduct(Long productId, int pageNum, int pageSize);

    /** 当前会员是否还有可评论的已支付订单；orderNo 为空时按卡产品找最早一笔，传了则精确校验该订单 */
    boolean canComment(Long memberId, Long productId, String orderNo);

    Long create(Long memberId, CommentCreateRequest request);

    /** 我的评论：显示中与已屏蔽都返回，已删除不返回 */
    PageResult<MyCommentVO> pageByMember(Long memberId, int pageNum, int pageSize);

    void deleteByMember(Long memberId, Long id);

    /** 后台评论列表：所有状态都返回，便于留痕 */
    PageResult<AdminCommentVO> pageForAdmin(Long productId, Integer status, int pageNum, int pageSize);

    void updateStatus(Long id, Integer status);

    void deleteByAdmin(Long id);

    /** 店长回复；replyContent 为空表示清除回复 */
    void reply(Long id, Long staffId, String replyContent);
}
