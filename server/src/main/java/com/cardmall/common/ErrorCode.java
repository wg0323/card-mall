package com.cardmall.common;

public final class ErrorCode {

    private ErrorCode() {
    }

    public static final int PARAM_INVALID = 1000;
    public static final int APP_UNAUTHORIZED = 1001;

    /** 三端通用：access token 过期，前端用 refreshToken 换新后重试，不要直接踢回登录页 */
    public static final int TOKEN_EXPIRED = 1002;
    public static final int CARD_NOT_USABLE = 1101;
    public static final int ORDER_NOT_FOUND = 1102;
    public static final int ORDER_STATUS_INVALID = 1103;
    public static final int COMMENT_NOT_ELIGIBLE = 1104;
    public static final int COMMENT_NOT_FOUND = 1105;

    public static final int STAFF_UNAUTHORIZED = 2000;
    public static final int VERIFY_CODE_INVALID = 2001;
    public static final int VERIFY_TIMES_INVALID = 2002;
    public static final int CARD_TIMES_NOT_ENOUGH = 2003;

    public static final int ADMIN_UNAUTHORIZED = 3000;
    public static final int STAFF_ACCOUNT_INVALID = 3001;
    public static final int STAFF_PHONE_DUPLICATED = 3002;
    public static final int RESOURCE_NOT_FOUND = 3003;

    /** 文件上传：类型、大小、存储不可用等情况三端通用 */
    public static final int FILE_INVALID = 4001;
}
