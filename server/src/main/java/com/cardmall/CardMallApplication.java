package com.cardmall;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// 认证全部由 JWT 过滤器完成，不需要 Boot 生成的默认内存用户
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
// 用 markerInterface 而不是整包扫描：否则 WxAuthClient、PayChannel 这类业务接口也会被当成 Mapper 代理
@MapperScan(basePackages = "com.cardmall", markerInterface = BaseMapper.class)
public class CardMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardMallApplication.class, args);
    }
}
