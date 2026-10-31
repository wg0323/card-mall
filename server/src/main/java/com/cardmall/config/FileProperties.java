package com.cardmall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 对象存储配置。凭据与现有数据源一样只放 application-local.yml，主配置里留空。
 */
@Data
@Component
@ConfigurationProperties(prefix = "card-mall.file")
public class FileProperties {

    /** MinIO 服务地址，如 http://127.0.0.1:9000 */
    private String endpoint;

    private String accessKey;

    private String secretKey;

    /** 桶名 */
    private String bucket;

    /** 图片访问前缀，留空则用 endpoint；将来换 CDN 域名只改这一项 */
    private String publicBase;

    /** 单张图片大小上限（MB） */
    private Integer maxSizeMb;
}
