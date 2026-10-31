package com.cardmall.security;

/**
 * 三端身份。JWT 里带 type，用来隔离会员 / 店员 / 后台三种登录态。
 */
public enum LoginType {

    MEMBER("MEMBER"),
    STAFF("STAFF"),
    ADMIN("ADMIN");

    private final String authority;

    LoginType(String authority) {
        this.authority = authority;
    }

    public String authority() {
        return "ROLE_" + authority;
    }

    public static LoginType fromAuthority(String authority) {
        for (LoginType type : values()) {
            if (type.authority().equals(authority)) {
                return type;
            }
        }
        return MEMBER;
    }
}
