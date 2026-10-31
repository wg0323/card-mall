package com.cardmall.security;

import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 取当前登录人。业务代码只依赖这个门面，不直接碰 SecurityContext。
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static LoginUser get() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof LoginUser)) {
            throw new BizException(ErrorCode.APP_UNAUTHORIZED, "请先登录");
        }
        return (LoginUser) authentication.getPrincipal();
    }

    public static Long id() {
        return get().getUserId();
    }

    public static LoginType type() {
        return get().getLoginType();
    }

    public static Integer role() {
        return get().getRole();
    }
}
