package com.cardmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("verify_record")
public class VerifyRecord {

    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_REVERSED = 2;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String recordNo;

    /** 幂等键：一次确认动作生成一个，uk_request_id 兜底 */
    private String requestId;

    private Long memberCardId;

    private Long memberId;

    private Long staffId;

    private Long storeId;

    /** 本次扣减次数，期限型为 0 */
    private Integer verifyTimes;

    private Integer remainingBefore;

    private Integer remainingAfter;

    /** 1-正常，2-已撤销 */
    private Integer status;

    private String remark;

    private LocalDateTime verifyTime;

    private Long reverseStaffId;

    private LocalDateTime reverseTime;

    private String reverseReason;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}

