package com.cardmall.controller;

import com.cardmall.common.Result;
import com.cardmall.service.DashboardService;
import com.cardmall.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "后台-工作台")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "工作台数据")
    @ApiResponse(responseCode = "200", description = "成功")
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.ok(dashboardService.load());
    }
}
