package com.cardmall.wx;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.impl.WxMaDefaultConfigImpl;
import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;

@Slf4j
public class HttpWxAuthClient implements WxAuthClient {

    private final WxMaService wxMaService;

    public HttpWxAuthClient(String appId, String appSecret) {
        WxMaDefaultConfigImpl config = new WxMaDefaultConfigImpl();
        config.setAppid(appId);
        config.setSecret(appSecret);
        WxMaServiceImpl service = new WxMaServiceImpl();
        service.setWxMaConfig(config);
        this.wxMaService = service;
    }

    @Override
    public String code2OpenId(String code) {
        try {
            return wxMaService.getUserService().getSessionInfo(code).getOpenid();
        } catch (WxErrorException e) {
            log.warn("wx code2Session failed, code={}", code, e);
            throw new BizException(ErrorCode.APP_UNAUTHORIZED, "微信登录失败，请重试");
        }
    }

    @Override
    public String code2Phone(String code) {
        try {
            return wxMaService.getUserService().getPhoneNoInfo(code).getPhoneNumber();
        } catch (WxErrorException e) {
            log.warn("wx getPhoneNoInfo failed, code={}", code, e);
            throw new BizException(ErrorCode.APP_UNAUTHORIZED, "获取手机号失败，请重试");
        }
    }
}
