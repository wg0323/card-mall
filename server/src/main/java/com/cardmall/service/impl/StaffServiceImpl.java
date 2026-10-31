package com.cardmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import com.cardmall.common.FileUrls;
import com.cardmall.common.PageResult;
import com.cardmall.dto.StaffCreateRequest;
import com.cardmall.entity.Staff;
import com.cardmall.mapper.StaffMapper;
import com.cardmall.service.StaffService;
import com.cardmall.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaffServiceImpl extends ServiceImpl<StaffMapper, Staff> implements StaffService {

    /** M1 统一初始密码，首次登录后修改 */
    private static final String INITIAL_PASSWORD = "123456";

    private final PasswordEncoder passwordEncoder;

    private final StoreService storeService;

    /**
     * 校验手机号与密码，失败统一抛一个错误码，不暴露"手机号是否存在"。
     */
    @Override
    public Staff authenticate(String phone, String password) {
        Staff staff = baseMapper.selectOne(new QueryWrapper<Staff>().eq("phone", phone));
        if (staff == null || !passwordEncoder.matches(password, staff.getPassword())) {
            throw new BizException(ErrorCode.STAFF_ACCOUNT_INVALID, "账号或密码错误");
        }
        if (staff.getStatus() == null || staff.getStatus() != 1) {
            throw new BizException(ErrorCode.STAFF_ACCOUNT_INVALID, "账号已停用");
        }
        return staff;
    }

    @Override
    public PageResult<Staff> pageStaffs(int pageNum, int pageSize) {
        QueryWrapper<Staff> qw = new QueryWrapper<Staff>().orderByAsc("id");
        Page<Staff> result = baseMapper.selectPage(new Page<Staff>(pageNum, pageSize), qw);
        return PageResult.of(result.getRecords(), result.getTotal());
    }

    @Override
    public Long create(StaffCreateRequest request) {
        Staff staff = new Staff();
        staff.setStoreId(storeService.getMainStore().getId());
        staff.setName(request.getName());
        staff.setPhone(request.getPhone());
        staff.setPassword(passwordEncoder.encode(INITIAL_PASSWORD));
        staff.setRole(request.getRole());
        staff.setStatus(1);
        try {
            baseMapper.insert(staff);
        } catch (DuplicateKeyException e) {
            throw new BizException(ErrorCode.STAFF_PHONE_DUPLICATED, "该手机号已被使用");
        }
        return staff.getId();
    }

    @Override
    public void changePassword(Long staffId, String oldPassword, String newPassword) {
        Staff staff = baseMapper.selectById(staffId);
        if (staff == null || !passwordEncoder.matches(oldPassword, staff.getPassword())) {
            throw new BizException(ErrorCode.STAFF_ACCOUNT_INVALID, "原密码错误");
        }
        Staff update = new Staff();
        update.setId(staffId);
        update.setPassword(passwordEncoder.encode(newPassword));
        baseMapper.updateById(update);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "状态只能是 0 或 1");
        }
        Staff existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND, "员工不存在");
        }
        Staff update = new Staff();
        update.setId(id);
        update.setStatus(status);
        baseMapper.updateById(update);
    }

    @Override
    public Staff getProfile(Long staffId) {
        Staff staff = baseMapper.selectById(staffId);
        if (staff == null) {
            throw new BizException(ErrorCode.STAFF_UNAUTHORIZED, "账号不存在，请重新登录");
        }
        return staff;
    }

    @Override
    public void updateAvatar(Long staffId, String avatar) {
        Staff existing = getProfile(staffId);
        Staff update = new Staff();
        update.setId(existing.getId());
        update.setAvatar(FileUrls.normalize(avatar));
        baseMapper.updateById(update);
    }
}
