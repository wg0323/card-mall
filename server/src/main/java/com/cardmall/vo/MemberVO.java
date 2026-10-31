package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会员出参。不含 openId / unionId：微信标识只在服务端使用，不出接口。
 */
@Data
@Schema(description = "会员信息")
public class MemberVO {

    @Schema(description = "会员ID")
    private Long id;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "手机号，未绑定时为空串")
    private String phone;

    @Schema(description = "最近登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "注册时间")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
}
