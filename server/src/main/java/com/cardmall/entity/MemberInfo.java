package com.cardmall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("member_info")
public class MemberInfo {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String openId;

    private String unionId;

    private String nickname;

    private String avatar;

    private String phone;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
