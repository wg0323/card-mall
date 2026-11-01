package com.cardmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Schema(description = "发表评论请求")
public class CommentCreateRequest {

    @Schema(description = "卡产品ID")
    @NotNull(message = "卡产品不能为空")
    private Long productId;

    @Schema(description = "来源订单号；从订单详情进入时传，卡详情进入不传")
    @Size(max = 32, message = "订单号过长")
    private String orderNo;

    @Schema(description = "评论内容，1-500 字")
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 500, message = "评论内容最多 500 字")
    private String content;

    @Schema(description = "图片地址，最多 4 张")
    @Size(max = 4, message = "最多上传 4 张图片")
    private List<String> images;
}
