package com.cardmall.wx;

/**
 * 仅本地开发与自动化测试使用：不请求微信服务器。
 * 上线前必须把 card-mall.wx.mock 置为 false 并配好 AppID/AppSecret。
 */
public class MockWxAuthClient implements WxAuthClient {

    private static final String OPEN_ID_PREFIX = "mock_";
    private static final String MOCK_PHONE_PREFIX = "139";

    @Override
    public String code2OpenId(String code) {
        return OPEN_ID_PREFIX + code;
    }

    @Override
    public String code2Phone(String code) {
        // 生成稳定的 11 位手机号，避免与种子管理员 13800000000 冲突
        int tail = Math.abs(code.hashCode()) % 100000000;
        return MOCK_PHONE_PREFIX + String.format("%08d", tail);
    }
}
