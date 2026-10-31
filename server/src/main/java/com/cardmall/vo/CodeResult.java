package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "核销码")
public class CodeResult {

    @Schema(description = "核销码 token，展示为二维码；预览与确认核销都用它")
    private String token;

    @Schema(description = "有效期秒数，前端据此倒计时并自动换码")
    private long expireIn;
}
