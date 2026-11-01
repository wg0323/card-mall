package com.cardmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("card_comment")
public class CardComment {

    public static final int STATUS_VISIBLE = 1;
    public static final int STATUS_HIDDEN = 2;
    public static final int STATUS_DELETED = 3;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;

    /** 卡名称快照，卡产品改名后评论仍显示购买时的名称 */
    private String productName;

    private Long memberId;

    /** 来源订单号，uk_order_no 保证一笔订单只能评论一次 */
    private String orderNo;

    private String content;

    /** 图片地址逗号分隔，最多 4 张 */
    private String images;

    /** 1-显示，2-已屏蔽，3-已删除 */
    private Integer status;

    private String replyContent;

    private Long replyStaffId;

    private LocalDateTime replyTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
