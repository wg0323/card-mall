package com.cardmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "评论状态变更请求")
public class CommentStatusRequest {

    @Schema(description = "状态：1-显示，2-已屏蔽")
    @NotNull(message = "状态不能为空")
    @Min(value = 1, message = "状态只能是 1 或 2")
    @Max(value = 2, message = "状态只能是 1 或 2")
    private Integer status;
}
