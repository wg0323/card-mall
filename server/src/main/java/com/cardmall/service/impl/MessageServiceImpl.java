package com.cardmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cardmall.entity.MemberMessage;
import com.cardmall.mapper.MemberMessageMapper;
import com.cardmall.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl extends ServiceImpl<MemberMessageMapper, MemberMessage> implements MessageService {

    private static final String COMMENT_REPLY_TITLE = "店长回复了你的评价";

    @Override
    public void createCommentReplyMessage(Long memberId, Long commentId, String replyContent) {
        MemberMessage message = new MemberMessage();
        message.setMemberId(memberId);
        message.setType(MemberMessage.TYPE_COMMENT_REPLY);
        message.setTitle(COMMENT_REPLY_TITLE);
        message.setContent(replyContent);
        message.setBizId(commentId);
        message.setIsRead(MemberMessage.READ_NO);
        try {
            baseMapper.insert(message);
        } catch (DuplicateKeyException e) {
            // uk_type_biz_id 兜底：同一条评论只提醒一次，重复回复直接忽略
            log.warn("评论 {} 已有回复消息，跳过重复提醒", commentId);
        }
    }

    @Override
    public long countUnread(Long memberId) {
        Long count = baseMapper.selectCount(new QueryWrapper<MemberMessage>()
                .eq("member_id", memberId)
                .eq("is_read", MemberMessage.READ_NO));
        return count == null ? 0L : count;
    }

    @Override
    public void markReadByCommentIds(Long memberId, List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return;
        }
        baseMapper.update(null, new UpdateWrapper<MemberMessage>()
                .eq("member_id", memberId)
                .eq("type", MemberMessage.TYPE_COMMENT_REPLY)
                .eq("is_read", MemberMessage.READ_NO)
                .in("biz_id", commentIds)
                .set("is_read", MemberMessage.READ_YES)
                .set("read_time", LocalDateTime.now()));
    }

    @Override
    public List<Long> listUnreadCommentIds(Long memberId, List<Long> commentIds) {
        if (commentIds == null || commentIds.isEmpty()) {
            return Collections.emptyList();
        }
        List<MemberMessage> messages = baseMapper.selectList(new QueryWrapper<MemberMessage>()
                .select("biz_id")
                .eq("member_id", memberId)
                .eq("type", MemberMessage.TYPE_COMMENT_REPLY)
                .eq("is_read", MemberMessage.READ_NO)
                .in("biz_id", commentIds));
        List<Long> ids = new ArrayList<Long>();
        for (MemberMessage message : messages) {
            ids.add(message.getBizId());
        }
        return ids;
    }
}
