package com.cardmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("member_message")
public class MemberMessage {

    /** 评论回复消息 */
    public static final int TYPE_COMMENT_REPLY = 1;

    public static final int READ_YES = 1;

    public static final int READ_NO = 0;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 接收会员ID */
    private Long memberId;

    /** 消息类型：1-评论回复 */
    private Integer type;

    private String title;

    private String content;

    /** 业务主键：type=1 时为评论ID，配合 uk_type_biz_id 保证一条评论只提醒一次 */
    private Long bizId;

    /** 是否已读：1-是，0-否 */
    private Integer isRead;

    private LocalDateTime readTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
