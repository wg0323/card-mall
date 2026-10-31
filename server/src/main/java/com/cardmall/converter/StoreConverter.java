package com.cardmall.converter;

import com.cardmall.common.FileUrls;
import com.cardmall.dto.StoreSaveRequest;
import com.cardmall.entity.Store;
import com.cardmall.vo.StoreVO;

/**
 * 对象转换统一放这里，Controller/Service 里不做字段级拷贝。
 */
public final class StoreConverter {

    private StoreConverter() {
    }

    public static void merge(StoreSaveRequest request, Store target) {
        target.setName(request.getName());
        target.setLogo(FileUrls.normalize(request.getLogo()));
        target.setPhone(emptyIfNull(request.getPhone()));
        target.setAddress(emptyIfNull(request.getAddress()));
        target.setLongitude(request.getLongitude());
        target.setLatitude(request.getLatitude());
        target.setBusinessHours(emptyIfNull(request.getBusinessHours()));
    }

    public static StoreVO toVO(Store store) {
        if (store == null) {
            return null;
        }
        StoreVO vo = new StoreVO();
        vo.setId(store.getId());
        vo.setName(store.getName());
        vo.setLogo(FileUrls.resolve(store.getLogo()));
        vo.setPhone(store.getPhone());
        vo.setAddress(store.getAddress());
        vo.setLongitude(store.getLongitude());
        vo.setLatitude(store.getLatitude());
        vo.setBusinessHours(store.getBusinessHours());
        return vo;
    }

    private static String emptyIfNull(String value) {
        return value == null ? "" : value;
    }
}
