package com.cardmall.controller;

import com.cardmall.common.PageResult;
import com.cardmall.common.Result;
import com.cardmall.dto.CommentReplyRequest;
import com.cardmall.dto.CommentStatusRequest;
import com.cardmall.security.CurrentUser;
import com.cardmall.service.CommentService;
import com.cardmall.vo.AdminCommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "后台-评论")
@RestController
@RequestMapping("/api/admin/comment")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;

    @Operation(summary = "评论分页")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/page")
    public Result<PageResult<AdminCommentVO>> page(
            @Parameter(description = "卡产品ID") @RequestParam(required = false) Long productId,
            @Parameter(description = "状态：1-显示，2-已屏蔽，3-已删除") @RequestParam(required = false) Integer status,
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(commentService.pageForAdmin(productId, status, pageNum, pageSize));
    }

    @Operation(summary = "屏蔽/恢复评论")
    @ApiResponse(responseCode = "200", description = "成功")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@Parameter(description = "评论ID") @PathVariable Long id,
                                     @RequestBody @Valid CommentStatusRequest request) {
        commentService.updateStatus(id, request.getStatus());
        return Result.ok();
    }

    @Operation(summary = "删除评论（软删除，后台留痕）")
    @ApiResponse(responseCode = "200", description = "成功")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "评论ID") @PathVariable Long id) {
        commentService.deleteByAdmin(id);
        return Result.ok();
    }

    @Operation(summary = "回复评论，传空串清除回复")
    @ApiResponse(responseCode = "200", description = "成功")
    @PutMapping("/{id}/reply")
    public Result<Void> reply(@Parameter(description = "评论ID") @PathVariable Long id,
                              @RequestBody @Valid CommentReplyRequest request) {
        commentService.reply(id, CurrentUser.id(), request.getReplyContent());
        return Result.ok();
    }
}
