package com.cardmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import com.cardmall.common.PageResult;
import com.cardmall.converter.CardProductConverter;
import com.cardmall.dto.CardProductSaveRequest;
import com.cardmall.entity.CardProduct;
import com.cardmall.mapper.CardProductMapper;
import com.cardmall.service.CardProductService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CardProductServiceImpl extends ServiceImpl<CardProductMapper, CardProduct> implements CardProductService {

    @Override
    public PageResult<CardProduct> pageAdmin(int pageNum, int pageSize) {
        QueryWrapper<CardProduct> qw = new QueryWrapper<CardProduct>()
                .orderByDesc("sort").orderByDesc("id");
        Page<CardProduct> result = baseMapper.selectPage(new Page<CardProduct>(pageNum, pageSize), qw);
        return PageResult.of(result.getRecords(), result.getTotal());
    }

    @Override
    public List<CardProduct> listOnSale() {
        return baseMapper.selectList(new QueryWrapper<CardProduct>()
                .eq("status", 1)
                .orderByDesc("sort").orderByDesc("id"));
    }

    @Override
    public CardProduct getProductById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public CardProduct getOnSaleById(Long id) {
        CardProduct product = baseMapper.selectById(id);
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            return null;
        }
        return product;
    }

    @Override
    public CardProduct getOnSaleOrThrow(Long id) {
        CardProduct product = getOnSaleById(id);
        if (product == null) {
            throw new BizException(ErrorCode.CARD_NOT_USABLE, "卡产品不存在或已下架");
        }
        return product;
    }

    @Override
    public Long create(CardProductSaveRequest request) {
        validate(request);
        CardProduct product = new CardProduct();
        CardProductConverter.merge(request, product);
        baseMapper.insert(product);
        return product.getId();
    }

    @Override
    public void update(Long id, CardProductSaveRequest request) {
        validate(request);
        CardProduct existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND, "卡产品不存在");
        }
        CardProductConverter.merge(request, existing);
        baseMapper.updateById(existing);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "状态只能是 0 或 1");
        }
        CardProduct existing = baseMapper.selectById(id);
        if (existing == null) {
            throw new BizException(ErrorCode.RESOURCE_NOT_FOUND, "卡产品不存在");
        }
        CardProduct update = new CardProduct();
        update.setId(id);
        update.setStatus(status);
        baseMapper.updateById(update);
    }

    /**
     * 计次型必须给出总次数与单次原价：退款规则要用单次原价折算已消费部分。
     */
    private void validate(CardProductSaveRequest request) {
        if (request.getCardType() == null) {
            return;
        }
        if (request.getCardType() == CardProduct.TYPE_COUNT) {
            if (request.getTotalTimes() == null || request.getTotalTimes() < 1) {
                throw new BizException(ErrorCode.PARAM_INVALID, "计次型必须填写总次数");
            }
            if (request.getUnitPrice() == null
                    || request.getUnitPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException(ErrorCode.PARAM_INVALID, "计次型必须填写单次原价");
            }
        }
    }
}
