package com.cardmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema(description = "后台修改密码请求")
public class ChangePasswordRequest {

    @Schema(description = "原密码")
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @Schema(description = "新密码，6 到 32 位")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "新密码长度需在 6 到 32 位之间")
    private String newPassword;
}
