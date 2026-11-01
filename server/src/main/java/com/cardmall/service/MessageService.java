package com.cardmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cardmall.entity.MemberMessage;

import java.util.List;

public interface MessageService extends IService<MemberMessage> {

    /** 店长回复评论时给会员留一条未读消息；同一条评论重复回复不会产生第二条 */
    void createCommentReplyMessage(Long memberId, Long commentId, String replyContent);

    /** 当前会员未读消息数 */
    long countUnread(Long memberId);

    /** 按评论ID把本人的回复消息标记已读，重复调用无副作用 */
    void markReadByCommentIds(Long memberId, List<Long> commentIds);

    /** 在这一批评论里，当前会员还有未读回复的评论ID */
    List<Long> listUnreadCommentIds(Long memberId, List<Long> commentIds);
}
