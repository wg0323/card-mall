package com.cardmall.security;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 认证通过后放进 SecurityContext 的主体。只放 id 与端类型，不放密码等敏感信息。
 */
@Getter
@AllArgsConstructor
public class LoginUser {

    private final Long userId;

    private final LoginType loginType;

    /** 店员/后台的角色：1-老板，2-店长，3-店员；会员为 null */
    private final Integer role;
}
