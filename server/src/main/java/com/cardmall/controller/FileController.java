package com.cardmall.controller;

import com.cardmall.common.FileBizType;
import com.cardmall.common.Result;
import com.cardmall.service.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 三端上传入口分开，权限由 SecurityConfig 里三条 JWT 链各自负责；
 * 会员与店员只能传自己用途的图，管理端可传卡封面、门店 Logo、员工头像。
 */
@Tag(name = "文件上传")
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileStorageService fileStorageService;

    @Operation(summary = "管理端上传图片")
    @ApiResponse(responseCode = "200", description = "成功，返回图片访问地址")
    @PostMapping("/api/admin/file")
    public Result<String> adminUpload(
            @Parameter(description = "用途：card-卡封面，store-门店Logo，staff-员工头像", required = true)
            @RequestParam String bizType,
            @RequestPart("file") MultipartFile file) {
        return Result.ok(fileStorageService.upload(file, FileBizType.of(bizType)));
    }

    @Operation(summary = "会员端上传图片")
    @ApiResponse(responseCode = "200", description = "成功，返回图片访问地址")
    @PostMapping("/api/app/file")
    public Result<String> appUpload(@RequestPart("file") MultipartFile file) {
        return Result.ok(fileStorageService.upload(file, FileBizType.MEMBER));
    }

    @Operation(summary = "店员端上传图片")
    @ApiResponse(responseCode = "200", description = "成功，返回图片访问地址")
    @PostMapping("/api/staff/file")
    public Result<String> staffUpload(@RequestPart("file") MultipartFile file) {
        return Result.ok(fileStorageService.upload(file, FileBizType.STAFF));
    }
}
