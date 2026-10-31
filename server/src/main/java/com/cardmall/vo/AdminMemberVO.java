package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台会员列表项：会员基本信息 + 持卡与核销统计。
 * 单独一个 VO 而不是复用 MemberVO，是为了让「统计字段」只出现在后台列表接口里。
 */
@Data
@Schema(description = "后台会员列表项")
public class AdminMemberVO {

    @Schema(description = "会员ID")
    private Long id;

    @Schema(description = "昵称，mock 注册的会员可能为空")
    private String nickname;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "手机号，未绑定时为空串")
    private String phone;

    @Schema(description = "持卡数，含已用完与已过期的卡")
    private Integer cardCount;

    @Schema(description = "累计核销次数，按正常状态的核销记录累加")
    private Integer totalVerifyTimes;

    @Schema(description = "最近登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "注册时间")
    private LocalDateTime createTime;
}
