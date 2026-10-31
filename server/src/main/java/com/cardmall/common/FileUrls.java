package com.cardmall.common;

import com.cardmall.config.FileProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 库里存 objectKey，出参拼访问地址、入参剥回 key，两边都兼容历史数据里直接写的外链。
 * Converter 是静态工具类，改成注入会波及所有调用点，因此这里把前缀放进静态字段。
 */
@Component
public class FileUrls {

    private static final String HTTP_PREFIX = "http://";

    private static final String HTTPS_PREFIX = "https://";

    private static String base = "";

    private static String bucket = "";

    public FileUrls(FileProperties properties) {
        base = trimEnd(StringUtils.hasText(properties.getPublicBase())
                ? properties.getPublicBase()
                : properties.getEndpoint());
        bucket = properties.getBucket() == null ? "" : properties.getBucket();
    }

    /**
     * key 转可访问地址；已是完整外链或未配置前缀时原样返回。
     */
    public static String resolve(String keyOrUrl) {
        if (!StringUtils.hasText(keyOrUrl)) {
            return "";
        }
        if (isAbsolute(keyOrUrl) || !StringUtils.hasText(base)) {
            return keyOrUrl;
        }
        return base + "/" + bucket + "/" + trimStart(keyOrUrl);
    }

    /**
     * 前端回传的地址归一化成 key，避免把完整 URL 写进库；第三方外链原样保留。
     */
    public static String normalize(String urlOrKey) {
        if (!StringUtils.hasText(urlOrKey)) {
            return "";
        }
        if (!isAbsolute(urlOrKey)) {
            return trimStart(urlOrKey);
        }
        String prefix = base + "/" + bucket + "/";
        return urlOrKey.startsWith(prefix) ? urlOrKey.substring(prefix.length()) : urlOrKey;
    }

    private static boolean isAbsolute(String value) {
        return value.startsWith(HTTP_PREFIX) || value.startsWith(HTTPS_PREFIX);
    }

    private static String trimStart(String value) {
        return value.startsWith("/") ? value.substring(1) : value;
    }

    private static String trimEnd(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
