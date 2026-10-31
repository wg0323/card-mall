package com.cardmall.wx;

public interface WxAuthClient {

    /**
     * 用 wx.login 拿到的 code 换 openid。
     */
    String code2OpenId(String code);

    /**
     * 用手机号快速验证组件返回的 code 换手机号。
     */
    String code2Phone(String code);
}
