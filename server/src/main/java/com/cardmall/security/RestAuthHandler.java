package com.cardmall.security;

import com.cardmall.common.ErrorCode;
import com.cardmall.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 未登录与越权都返回统一响应体，并按 URI 前缀映射到各端错误码：
 * 这样前端不用看 HTTP 状态码猜是哪一端的问题。
 */
@Component
@RequiredArgsConstructor
public class RestAuthHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException e) throws IOException {
        if (Boolean.TRUE.equals(request.getAttribute(JwtAuthenticationFilter.AUTH_EXPIRED_ATTRIBUTE))) {
            write(response, ErrorCode.TOKEN_EXPIRED, "登录已过期");
            return;
        }
        write(response, codeOf(request), "请先登录");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException e) throws IOException {
        write(response, codeOf(request), "没有该端口的访问权限");
    }

    private int codeOf(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri.startsWith("/api/staff/")) {
            return ErrorCode.STAFF_UNAUTHORIZED;
        }
        if (uri.startsWith("/api/admin/")) {
            return ErrorCode.ADMIN_UNAUTHORIZED;
        }
        return ErrorCode.APP_UNAUTHORIZED;
    }

    private void write(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(Result.fail(code, message)));
    }
}
