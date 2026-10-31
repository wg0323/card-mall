package com.cardmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@Schema(description = "会员资料更新请求")
public class MemberProfileUpdateRequest {

    @Schema(description = "头像地址，不传则不改", example = "member/2026/10/9f3e.jpg")
    @Size(max = 255, message = "头像地址过长")
    private String avatar;

    @Schema(description = "昵称，不传则不改", example = "洗车常客")
    @Size(max = 32, message = "昵称最多 32 个字符")
    private String nickname;
}
