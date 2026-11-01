package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "我的评论")
public class MyCommentVO {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "卡产品ID")
    private Long productId;

    @Schema(description = "卡名称快照")
    private String productName;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "图片地址，最多 4 张")
    private List<String> images;

    @Schema(description = "状态：1-显示，2-已屏蔽")
    private Integer status;

    @Schema(description = "店长回复内容，未回复时为空")
    private String replyContent;

    @Schema(description = "店长回复时间")
    private LocalDateTime replyTime;

    @Schema(description = "评论时间")
    private LocalDateTime createTime;
}
