package com.cardmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cardmall.common.PageResult;
import com.cardmall.dto.StaffCreateRequest;
import com.cardmall.entity.Staff;

public interface StaffService extends IService<Staff> {

    /** 校验手机号与密码，失败统一抛一个错误码，不暴露手机号是否存在 */
    Staff authenticate(String phone, String password);

    PageResult<Staff> pageStaffs(int pageNum, int pageSize);

    Long create(StaffCreateRequest request);

    void changePassword(Long staffId, String oldPassword, String newPassword);

    void updateStatus(Long id, Integer status);

    /** 当前登录人资料；不存在时抛未登录，与令牌失效同语义 */
    Staff getProfile(Long staffId);

    /** 改头像，管理端与店员自用同一个口子 */
    void updateAvatar(Long staffId, String avatar);
}
