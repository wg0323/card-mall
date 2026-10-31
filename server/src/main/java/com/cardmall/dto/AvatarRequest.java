package com.cardmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema(description = "头像更新请求")
public class AvatarRequest {

    @Schema(description = "头像地址", example = "staff/2026/10/9f3e.jpg")
    @NotBlank(message = "头像不能为空")
    @Size(max = 255, message = "头像地址过长")
    private String avatar;
}
