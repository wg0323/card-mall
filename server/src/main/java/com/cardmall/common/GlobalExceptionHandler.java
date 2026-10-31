package com.cardmall.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldError() == null
                ? "参数不合法"
                : e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.fail(ErrorCode.PARAM_INVALID, msg);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleUnreadable(HttpMessageNotReadableException e) {
        return Result.fail(ErrorCode.PARAM_INVALID, "请求体格式错误");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleUploadSize(MaxUploadSizeExceededException e) {
        return Result.fail(ErrorCode.FILE_INVALID, "图片超过大小限制");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("unhandled error", e);
        return Result.fail(500, "服务异常，请稍后重试");
    }
}
