package com.cardmall.converter;

import com.cardmall.common.FileUrls;
import com.cardmall.dto.CardProductSaveRequest;
import com.cardmall.entity.CardProduct;
import com.cardmall.vo.CardProductVO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 对象转换统一放这里，Controller/Service 里不做字段级拷贝。
 */
public final class CardProductConverter {

    private CardProductConverter() {
    }

    public static void merge(CardProductSaveRequest request, CardProduct target) {
        target.setName(request.getName());
        target.setCardType(request.getCardType());
        target.setBizTag(emptyIfNull(request.getBizTag()));
        target.setPrice(request.getPrice());
        boolean countCard = request.getCardType() != null && request.getCardType() == CardProduct.TYPE_COUNT;
        target.setUnitPrice(countCard ? request.getUnitPrice() : BigDecimal.ZERO);
        target.setTotalTimes(countCard ? request.getTotalTimes() : 0);
        target.setValidDays(request.getValidDays());
        target.setCoverImage(FileUrls.normalize(request.getCoverImage()));
        target.setApplicableService(emptyIfNull(request.getApplicableService()));
        target.setPurchaseNotice(emptyIfNull(request.getPurchaseNotice()));
        target.setStatus(request.getStatus());
        target.setSort(request.getSort() == null ? 0 : request.getSort());
    }

    public static CardProductVO toVO(CardProduct product) {
        if (product == null) {
            return null;
        }
        CardProductVO vo = new CardProductVO();
        vo.setId(product.getId());
        vo.setName(product.getName());
        vo.setCardType(product.getCardType());
        vo.setBizTag(product.getBizTag());
        vo.setPrice(product.getPrice());
        vo.setUnitPrice(product.getUnitPrice());
        vo.setTotalTimes(product.getTotalTimes());
        vo.setValidDays(product.getValidDays());
        vo.setCoverImage(FileUrls.resolve(product.getCoverImage()));
        vo.setApplicableService(product.getApplicableService());
        vo.setPurchaseNotice(product.getPurchaseNotice());
        vo.setStatus(product.getStatus());
        vo.setSort(product.getSort());
        vo.setCreateTime(product.getCreateTime());
        vo.setUpdateTime(product.getUpdateTime());
        return vo;
    }

    public static List<CardProductVO> toVOList(List<CardProduct> products) {
        if (products == null) {
            return new ArrayList<CardProductVO>();
        }
        return products.stream().map(CardProductConverter::toVO).collect(Collectors.toList());
    }

    private static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }
}
