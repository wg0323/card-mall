package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工出参。不含 password：BCrypt 密文不出接口。
 */
@Data
@Schema(description = "员工信息")
public class StaffVO {

    @Schema(description = "员工ID")
    private Long id;

    @Schema(description = "所属门店ID")
    private Long storeId;

    @Schema(description = "员工姓名")
    private String name;

    @Schema(description = "登录手机号")
    private String phone;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "角色：1-老板，2-店长，3-店员")
    private Integer role;

    @Schema(description = "状态：0-停用，1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "修改时间")
    private LocalDateTime updateTime;
}
