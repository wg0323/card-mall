package com.cardmall.controller;

import com.cardmall.common.Result;
import com.cardmall.dto.MessageReadRequest;
import com.cardmall.security.CurrentUser;
import com.cardmall.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "会员端-站内消息")
@RestController
@RequestMapping("/api/app/message")
@RequiredArgsConstructor
public class AppMessageController {

    private final MessageService messageService;

    @Operation(summary = "未读消息数")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.ok(messageService.countUnread(CurrentUser.id()));
    }

    @Operation(summary = "按评论ID标记回复消息已读")
    @ApiResponse(responseCode = "200", description = "成功")
    @PutMapping("/read")
    public Result<Void> read(@RequestBody MessageReadRequest request) {
        messageService.markReadByCommentIds(CurrentUser.id(), request.getCommentIds());
        return Result.ok();
    }
}
