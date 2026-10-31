package com.cardmall.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import com.cardmall.converter.StoreConverter;
import com.cardmall.dto.StoreSaveRequest;
import com.cardmall.entity.Store;
import com.cardmall.mapper.StoreMapper;
import com.cardmall.service.StoreService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StoreServiceImpl extends ServiceImpl<StoreMapper, Store> implements StoreService {

    @Value("${card-mall.store-id:1}")
    private Long mainStoreId;

    @Override
    public Store getMainStore() {
        Store store = getById(mainStoreId);
        if (store == null) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND, "门店不存在，请检查数据库迁移");
        }
        return store;
    }

    @Override
    public void saveStore(StoreSaveRequest request) {
        Store store = getMainStore();
        StoreConverter.merge(request, store);
        updateById(store);
    }
}
