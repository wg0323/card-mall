package com.cardmall.pay;

import java.math.BigDecimal;

/**
 * 仅本地与自动化测试使用：不请求微信，直接视为支付成功。
 * 上线前把 card-mall.pay.channel 改成 wxpay 并实现 WxPayChannel。
 */
public class MockPayChannel implements PayChannel {

    private static final String CHANNEL = "MOCK";
    private static final String TRANSACTION_PREFIX = "MOCK";

    @Override
    public PrepayResult prepay(String orderNo, BigDecimal amount, String openId) {
        return new PrepayResult(CHANNEL, TRANSACTION_PREFIX + orderNo, true);
    }
}
