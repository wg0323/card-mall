package com.cardmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
@Schema(description = "回复评论请求")
public class CommentReplyRequest {

    @Schema(description = "回复内容，最多 500 字；传空串表示清除回复")
    @Size(max = 500, message = "回复内容最多 500 字")
    private String replyContent;
}
