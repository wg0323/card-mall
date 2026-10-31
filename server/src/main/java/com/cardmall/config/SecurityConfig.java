package com.cardmall.config;

import com.cardmall.security.JwtAuthenticationFilter;
import com.cardmall.security.RestAuthHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 三端三条链路，靠 JWT 里的端类型隔离：
 * 会员令牌打不开后台接口（→ 3000），未登录访问受保护接口 → 对应端的未登录码。
 * 每条链开头的 antMatcher 限定生效范围，否则第一条链会吃掉所有请求。
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RestAuthHandler restAuthHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain appChain(HttpSecurity http) throws Exception {
        return buildChain(http, "/api/app/**", "/api/app/auth/**", "MEMBER");
    }

    @Bean
    @Order(2)
    public SecurityFilterChain staffChain(HttpSecurity http) throws Exception {
        return buildChain(http, "/api/staff/**", "/api/staff/auth/**", "STAFF");
    }

    @Bean
    @Order(3)
    public SecurityFilterChain adminChain(HttpSecurity http) throws Exception {
        return buildChain(http, "/api/admin/**", "/api/admin/auth/**", "ADMIN");
    }

    @Bean
    @Order(4)
    public SecurityFilterChain publicChain(HttpSecurity http) throws Exception {
        http.antMatcher("/**")
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .anyRequest().permitAll();
        return http.build();
    }

    private SecurityFilterChain buildChain(HttpSecurity http, String prefix, String loginPath,
                                          String role) throws Exception {
        http.antMatcher(prefix)
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers(loginPath).permitAll()
                .anyRequest().hasRole(role)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(restAuthHandler)
                .accessDeniedHandler(restAuthHandler)
                .and()
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
