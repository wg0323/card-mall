package com.cardmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cardmall.dto.StoreSaveRequest;
import com.cardmall.entity.Store;

public interface StoreService extends IService<Store> {

    /** 单店模式：主门店固定 id=1，由 V2 迁移插入 */
    Store getMainStore();

    void saveStore(StoreSaveRequest request);
}
