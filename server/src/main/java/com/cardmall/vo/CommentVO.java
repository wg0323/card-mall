package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "卡产品评论")
public class CommentVO {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "卡产品ID")
    private Long productId;

    @Schema(description = "会员昵称，未设置时为空")
    private String memberNickname;

    @Schema(description = "会员头像地址")
    private String memberAvatar;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "图片地址，最多 4 张")
    private List<String> images;

    @Schema(description = "店长回复内容，未回复时为空")
    private String replyContent;

    @Schema(description = "店长回复时间")
    private LocalDateTime replyTime;

    @Schema(description = "评论时间")
    private LocalDateTime createTime;
}
