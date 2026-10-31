package com.cardmall.config;

import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

/**
 * JVM 默认时区固定为东八区：核销时间写的是 JVM 时间，
 * 而 create_time 由数据库生成，两边时区不一致会让“今日核销数”错开 8 小时。
 */
@Configuration
public class TimeZoneConfig {

    @PostConstruct
    public void initTimeZone() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
    }
}

