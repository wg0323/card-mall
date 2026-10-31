package com.cardmall.controller;

import com.cardmall.common.PageResult;
import com.cardmall.common.Result;
import com.cardmall.converter.StaffConverter;
import com.cardmall.dto.AvatarRequest;
import com.cardmall.dto.StaffCreateRequest;
import com.cardmall.service.StaffService;
import com.cardmall.vo.StaffVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "后台-员工")
@RestController
@RequestMapping("/api/admin/staff")
@RequiredArgsConstructor
public class AdminStaffController {

    private final StaffService staffService;

    @Operation(summary = "员工分页")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/page")
    public Result<PageResult<StaffVO>> page(
            @Parameter(description = "页码，从 1 开始") @RequestParam(defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int pageSize) {
        return Result.ok(staffService.pageStaffs(pageNum, pageSize).convert(StaffConverter::toVO));
    }

    @Operation(summary = "新增员工，初始密码 123456")
    @ApiResponse(responseCode = "200", description = "成功")
    @PostMapping
    public Result<Long> create(@RequestBody @Valid StaffCreateRequest request) {
        return Result.ok(staffService.create(request));
    }

    @Operation(summary = "启用/停用员工")
    @ApiResponse(responseCode = "200", description = "成功")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@Parameter(description = "员工ID") @PathVariable Long id,
                                     @Parameter(description = "状态：0-停用，1-启用") @RequestParam Integer status) {
        staffService.updateStatus(id, status);
        return Result.ok();
    }

    @Operation(summary = "设置员工头像")
    @ApiResponse(responseCode = "200", description = "成功")
    @PutMapping("/{id}/avatar")
    public Result<Void> updateAvatar(@Parameter(description = "员工ID") @PathVariable Long id,
                                     @RequestBody @Valid AvatarRequest request) {
        staffService.updateAvatar(id, request.getAvatar());
        return Result.ok();
    }
}
