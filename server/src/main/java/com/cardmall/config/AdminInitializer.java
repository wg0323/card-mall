package com.cardmall.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cardmall.entity.Staff;
import com.cardmall.mapper.StaffMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 首次启动时补一个老板账号。密码哈希不写进 SQL 迁移，避免手写 BCrypt 出错。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminInitializer implements ApplicationRunner {

    private static final String DEFAULT_PHONE = "13800000000";
    private static final String DEFAULT_PASSWORD = "123456";

    private final StaffMapper staffMapper;

    private final PasswordEncoder passwordEncoder;

    @Value("${card-mall.store-id:1}")
    private Long storeId;

    @Override
    public void run(ApplicationArguments args) {
        Long count = staffMapper.selectCount(new QueryWrapper<Staff>());
        if (count != null && count > 0) {
            return;
        }
        Staff staff = new Staff();
        staff.setStoreId(storeId);
        staff.setName("管理员");
        staff.setPhone(DEFAULT_PHONE);
        staff.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        staff.setRole(1);
        staff.setStatus(1);
        staffMapper.insert(staff);
        // 不把明文口令写进日志：日志经常被集中收集，等于把口令贴到服务器上
        log.warn("已创建默认管理员 {}，初始口令见 application-local.yml.example，请登录后立即修改", DEFAULT_PHONE);
    }
}
