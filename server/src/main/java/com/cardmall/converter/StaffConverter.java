package com.cardmall.converter;

import com.cardmall.common.FileUrls;
import com.cardmall.entity.Staff;
import com.cardmall.vo.StaffVO;

/**
 * 对象转换统一放这里，Controller/Service 里不做字段级拷贝。
 */
public final class StaffConverter {

    private StaffConverter() {
    }

    public static StaffVO toVO(Staff staff) {
        if (staff == null) {
            return null;
        }
        StaffVO vo = new StaffVO();
        vo.setId(staff.getId());
        vo.setStoreId(staff.getStoreId());
        vo.setName(staff.getName());
        vo.setPhone(staff.getPhone());
        vo.setAvatar(FileUrls.resolve(staff.getAvatar()));
        vo.setRole(staff.getRole());
        vo.setStatus(staff.getStatus());
        vo.setCreateTime(staff.getCreateTime());
        vo.setUpdateTime(staff.getUpdateTime());
        return vo;
    }
}
