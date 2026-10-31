package com.cardmall.service.impl;

import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import com.cardmall.common.FileUrls;
import com.cardmall.config.FileProperties;
import com.cardmall.service.FileStorageService;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MinIO 存储实现：首次上传时才建桶并设置公共读，避免启动阶段依赖对象存储可用。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioFileStorageImpl implements FileStorageService {

    private static final int BYTES_PER_MB = 1024 * 1024;

    private static final int DEFAULT_MAX_SIZE_MB = 5;

    private static final DateTimeFormatter YEAR_MONTH = DateTimeFormatter.ofPattern("yyyy/MM");

    /** Content-Type 白名单，扩展名由它推导，不采信客户端文件名 */
    private static final Map<String, String> ALLOWED_CONTENT_TYPES;

    static {
        Map<String, String> types = new HashMap<String, String>(8);
        types.put("image/jpeg", "jpg");
        // 部分客户端会把 jpg 报成 image/jpg，按 jpeg 的别名放行
        types.put("image/jpg", "jpg");
        types.put("image/png", "png");
        types.put("image/webp", "webp");
        ALLOWED_CONTENT_TYPES = Collections.unmodifiableMap(types);
    }

    private final MinioClient minioClient;

    private final FileProperties fileProperties;

    /** 桶只建一次，并发首传不会重复创建 */
    private final AtomicBoolean bucketReady = new AtomicBoolean(false);

    @Override
    public String upload(MultipartFile file, String bizType) {
        String extension = checkAndResolveExtension(file);
        String bucket = requireBucket();
        String objectKey = bizType + "/" + LocalDate.now().format(YEAR_MONTH) + "/" + newObjectName(extension);
        ensureBucket(bucket);
        try (InputStream input = file.getInputStream()) {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(input, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
        } catch (Exception e) {
            log.error("图片上传失败，bucket={}，objectKey={}", bucket, objectKey, e);
            throw new BizException(ErrorCode.FILE_INVALID, "图片上传失败，请稍后重试");
        }
        return FileUrls.resolve(objectKey);
    }

    private String checkAndResolveExtension(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ErrorCode.FILE_INVALID, "请选择要上传的图片");
        }
        int maxSizeMb = fileProperties.getMaxSizeMb() == null
                ? DEFAULT_MAX_SIZE_MB
                : fileProperties.getMaxSizeMb();
        if (file.getSize() > (long) maxSizeMb * BYTES_PER_MB) {
            throw new BizException(ErrorCode.FILE_INVALID, "图片不能超过 " + maxSizeMb + "MB");
        }
        String extension = ALLOWED_CONTENT_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new BizException(ErrorCode.FILE_INVALID, "只支持 JPG、PNG、WebP 格式的图片");
        }
        return extension;
    }

    private String requireBucket() {
        if (!StringUtils.hasText(fileProperties.getEndpoint()) || !StringUtils.hasText(fileProperties.getBucket())) {
            throw new BizException(ErrorCode.FILE_INVALID, "文件服务未配置");
        }
        return fileProperties.getBucket();
    }

    private void ensureBucket(String bucket) {
        if (bucketReady.get()) {
            return;
        }
        synchronized (bucketReady) {
            if (bucketReady.get()) {
                return;
            }
            try {
                if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
                    minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
                    minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                            .bucket(bucket)
                            .config(publicReadPolicy(bucket))
                            .build());
                    log.info("已创建对象存储桶并设为公共读：{}", bucket);
                }
                bucketReady.set(true);
            } catch (Exception e) {
                log.error("对象存储桶初始化失败，bucket={}", bucket, e);
                throw new BizException(ErrorCode.FILE_INVALID, "文件服务不可用");
            }
        }
    }

    private String publicReadPolicy(String bucket) {
        return "{\"Version\":\"2012-10-17\",\"Statement\":[{\"Effect\":\"Allow\","
                + "\"Principal\":{\"AWS\":[\"*\"]},\"Action\":[\"s3:GetObject\"],"
                + "\"Resource\":[\"arn:aws:s3:::" + bucket + "/*\"]}]}";
    }

    private String newObjectName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + "." + extension;
    }
}
