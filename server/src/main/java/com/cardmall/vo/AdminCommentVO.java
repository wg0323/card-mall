package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "后台评论")
public class AdminCommentVO {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "卡产品ID")
    private Long productId;

    @Schema(description = "卡名称快照")
    private String productName;

    @Schema(description = "会员ID")
    private Long memberId;

    @Schema(description = "会员昵称，未设置时为空")
    private String memberNickname;

    @Schema(description = "会员头像地址")
    private String memberAvatar;

    @Schema(description = "会员手机号，未绑定时为空")
    private String memberPhone;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "图片地址，最多 4 张")
    private List<String> images;

    @Schema(description = "状态：1-显示，2-已屏蔽，3-已删除")
    private Integer status;

    @Schema(description = "店长回复内容，未回复时为空")
    private String replyContent;

    @Schema(description = "店长回复时间")
    private LocalDateTime replyTime;

    @Schema(description = "评论时间")
    private LocalDateTime createTime;
}
