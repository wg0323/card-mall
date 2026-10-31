package com.cardmall.pay;

import java.math.BigDecimal;

public interface PayChannel {

    /**
     * 创建支付。M1 只有 mock 实现；M2 加 WxPayChannel 时业务代码不变。
     */
    PrepayResult prepay(String orderNo, BigDecimal amount, String openId);
}
