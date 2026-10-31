package com.cardmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("member_card")
public class MemberCard {

    public static final int TYPE_COUNT = 1;
    public static final int TYPE_PERIOD = 2;

    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_USED_UP = 2;
    public static final int STATUS_EXPIRED = 3;
    public static final int STATUS_FROZEN = 4;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String cardNo;

    private Long memberId;

    private Long productId;

    private String orderNo;

    private String cardName;

    /** 1-计次型，2-期限型 */
    private Integer cardType;

    private Integer totalTimes;

    private Integer remainingTimes;

    private LocalDateTime validStart;

    private LocalDateTime validEnd;

    /** 1-正常，2-已用完，3-已过期，4-已冻结 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
