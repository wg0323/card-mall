package com.cardmall.config;

import com.cardmall.pay.MockPayChannel;
import com.cardmall.pay.PayChannel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PayConfig {

    /**
     * M1 只有 mock；M2 加 WxPayChannel 时只改这里，业务代码不动。
     */
    @Bean
    public PayChannel payChannel(@Value("${card-mall.pay.channel:mock}") String channel) {
        if (!"mock".equalsIgnoreCase(channel)) {
            throw new IllegalStateException("支付渠道尚未实现：" + channel);
        }
        return new MockPayChannel();
    }
}
