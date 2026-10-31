package com.cardmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Schema(description = "确认核销请求")
public class VerifyConfirmRequest {

    @Schema(description = "扫码得到的核销码 token，一次性使用")
    @NotBlank(message = "核销码不能为空")
    private String token;

    @Schema(description = "幂等键，店员端一次确认动作生成一个 UUID")
    @NotBlank(message = "requestId 不能为空")
    @Size(max = 64, message = "requestId 过长")
    private String requestId;

    @Schema(description = "扣减次数，默认 1，上限取剩余次数与 10 的较小值")
    private Integer times;

    @Schema(description = "备注，选填")
    @Size(max = 200, message = "备注不能超过200字")
    private String remark;
}
