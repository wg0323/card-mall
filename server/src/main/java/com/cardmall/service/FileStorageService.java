package com.cardmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 图片存储。数据库只存 key，出参由 FileUrls 拼成访问地址。
 */
public interface FileStorageService {

    /**
     * 校验并转存图片，返回可直接访问的地址。
     *
     * @param file    前端上传的图片
     * @param bizType 用途，同时作为对象存储的目录前缀
     */
    String upload(MultipartFile file, String bizType);
}
