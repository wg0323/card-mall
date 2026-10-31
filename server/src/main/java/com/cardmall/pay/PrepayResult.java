package com.cardmall.pay;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PrepayResult {

    /** MOCK / WXPAY */
    private String channel;

    /** 渠道支付单号，用作流水唯一键 */
    private String transactionId;

    /** 是否已支付。mock 恒为 true；微信支付为 false，等回调 */
    private boolean paid;
}

