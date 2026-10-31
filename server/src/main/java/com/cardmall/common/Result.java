package com.cardmall.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "统一响应体")
public class Result<T> {

    @Schema(description = "业务码，0 为成功；1xxx 会员端、2xxx 店员端、3xxx 后台")
    private int code;

    @Schema(description = "提示信息，成功固定为 ok")
    private String message;

    @Schema(description = "业务数据，失败时为 null")
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<T>();
        r.code = 0;
        r.message = "ok";
        r.data = data;
        return r;
    }

    public static Result<Void> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<T>();
        r.code = code;
        r.message = message;
        return r;
    }
}
