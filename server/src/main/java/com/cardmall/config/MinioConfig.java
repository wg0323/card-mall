package com.cardmall.config;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * 只负责构建客户端，启动时不连 MinIO：没有对象存储时应用与集成测试照样能启动。
 * 真正的连通性校验推迟到首次上传（见 MinioFileStorageImpl）。
 */
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    /** 未配置时的占位地址，仅供客户端构建，不会发起连接 */
    private static final String PLACEHOLDER_ENDPOINT = "http://127.0.0.1:9000";

    private final FileProperties fileProperties;

    @Bean
    public MinioClient minioClient() {
        String endpoint = StringUtils.hasText(fileProperties.getEndpoint())
                ? fileProperties.getEndpoint()
                : PLACEHOLDER_ENDPOINT;
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(nullToEmpty(fileProperties.getAccessKey()), nullToEmpty(fileProperties.getSecretKey()))
                .build();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
