package com.cardmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@Schema(description = "绑定手机号请求")
public class BindPhoneRequest {

    @Schema(description = "getPhoneNumber 返回的 code，用于解密手机号")
    @NotBlank(message = "code 不能为空")
    private String code;
}
