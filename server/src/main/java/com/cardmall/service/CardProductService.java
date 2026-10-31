package com.cardmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cardmall.common.PageResult;
import com.cardmall.dto.CardProductSaveRequest;
import com.cardmall.entity.CardProduct;

import java.util.List;

public interface CardProductService extends IService<CardProduct> {

    PageResult<CardProduct> pageAdmin(int pageNum, int pageSize);

    /** 会员端首页：只要上架的卡 */
    List<CardProduct> listOnSale();

    CardProduct getProductById(Long id);

    /** 会员端只能买到上架的卡 */
    CardProduct getOnSaleById(Long id);

    /** 会员端卡详情：不存在或已下架直接抛业务异常，省得每个调用方都写一遍判空 */
    CardProduct getOnSaleOrThrow(Long id);

    Long create(CardProductSaveRequest request);

    void update(Long id, CardProductSaveRequest request);

    void updateStatus(Long id, Integer status);
}
