package com.cardmall.config;

import com.cardmall.wx.HttpWxAuthClient;
import com.cardmall.wx.MockWxAuthClient;
import com.cardmall.wx.WxAuthClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WxClientConfig {

    /**
     * 用显式开关而不是空 AppID 判断：@ConditionalOnProperty 匹配空串不可靠。
     */
    @Bean
    public WxAuthClient wxAuthClient(@Value("${card-mall.wx.mock:true}") boolean mock,
                                     @Value("${card-mall.wx.app-id:}") String appId,
                                     @Value("${card-mall.wx.app-secret:}") String appSecret) {
        if (mock) {
            return new MockWxAuthClient();
        }
        return new HttpWxAuthClient(appId, appSecret);
    }
}
