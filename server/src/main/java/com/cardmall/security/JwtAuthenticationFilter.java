package com.cardmall.security;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * 只负责把 JWT 解析成 Authentication 放进 SecurityContext；
 * 是否放行、需要什么角色，交给 SecurityConfig 里的三条链路判断。
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * 访问令牌过期的标记：认证入口点据此回 TOKEN_EXPIRED，前端好去刷新而不是直接踢回登录页。
     */
    public static final String AUTH_EXPIRED_ATTRIBUTE = "card-mall.auth.expired";

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (token != null) {
            try {
                LoginUser user = jwtTokenProvider.parseAccess(token);
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null,
                                Collections.singletonList(
                                        new SimpleGrantedAuthority(user.getLoginType().authority())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException e) {
                request.setAttribute(AUTH_EXPIRED_ATTRIBUTE, Boolean.TRUE);
                SecurityContextHolder.clearContext();
            } catch (RuntimeException e) {
                // 令牌无效就当匿名请求，由后续的认证入口点统一返回未登录
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || header.isEmpty()) {
            return null;
        }
        return header.startsWith(BEARER_PREFIX) ? header.substring(BEARER_PREFIX.length()) : header;
    }
}
