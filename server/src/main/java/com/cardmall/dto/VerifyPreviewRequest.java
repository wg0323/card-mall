package com.cardmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "核销码预览请求")
public class VerifyPreviewRequest {

    @Schema(description = "扫码得到的核销码 token")
    @NotBlank(message = "核销码不能为空")
    private String token;
}
