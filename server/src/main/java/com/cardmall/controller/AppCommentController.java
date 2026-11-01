package com.cardmall.controller;

import com.cardmall.common.PageResult;
import com.cardmall.common.Result;
import com.cardmall.dto.CommentCreateRequest;
import com.cardmall.security.CurrentUser;
import com.cardmall.service.CommentService;
import com.cardmall.vo.CommentVO;
import com.cardmall.vo.MyCommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "会员端-评论")
@RestController
@RequestMapping("/api/app/comment")
@RequiredArgsConstructor
public class AppCommentController {

    private final CommentService commentService;

    @Operation(summary = "卡产品评论分页")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/page")
    public Result<PageResult<CommentVO>> page(
            @Parameter(description = "卡产品ID") @RequestParam Long productId,
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(commentService.pageByProduct(productId, pageNum, pageSize));
    }

    @Operation(summary = "当前会员能否评论该卡产品")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/eligibility")
    public Result<Boolean> eligibility(
            @Parameter(description = "卡产品ID") @RequestParam Long productId,
            @Parameter(description = "订单号；从订单详情进入时传，精确校验该订单") @RequestParam(required = false) String orderNo) {
        return Result.ok(commentService.canComment(CurrentUser.id(), productId, orderNo));
    }

    @Operation(summary = "发表评论")
    @ApiResponse(responseCode = "200", description = "成功，返回评论ID")
    @PostMapping
    public Result<Long> create(@RequestBody @Valid CommentCreateRequest request) {
        return Result.ok(commentService.create(CurrentUser.id(), request));
    }

    @Operation(summary = "我的评论分页")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/mine/page")
    public Result<PageResult<MyCommentVO>> mine(
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(commentService.pageByMember(CurrentUser.id(), pageNum, pageSize));
    }

    @Operation(summary = "删除我的评论")
    @ApiResponse(responseCode = "200", description = "成功")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "评论ID") @PathVariable Long id) {
        commentService.deleteByMember(CurrentUser.id(), id);
        return Result.ok();
    }
}
