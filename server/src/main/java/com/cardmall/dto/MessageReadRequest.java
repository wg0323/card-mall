package com.cardmall.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "标记消息已读请求")
public class MessageReadRequest {

    @Schema(description = "评论ID列表，把这些评论对应的回复消息标记已读")
    private List<Long> commentIds;
}
