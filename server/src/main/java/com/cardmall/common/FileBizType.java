package com.cardmall.common;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 上传用途，同时作为对象存储的目录前缀。只接受白名单内的值，避免前端随便拼路径。
 */
public final class FileBizType {

    public static final String CARD = "card";

    public static final String STORE = "store";

    public static final String MEMBER = "member";

    public static final String STAFF = "staff";

    private static final Set<String> ALL = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(CARD, STORE, MEMBER, STAFF)));

    private FileBizType() {
    }

    public static String of(String value) {
        if (value == null || !ALL.contains(value)) {
            throw new BizException(ErrorCode.FILE_INVALID, "不支持的文件用途");
        }
        return value;
    }
}
