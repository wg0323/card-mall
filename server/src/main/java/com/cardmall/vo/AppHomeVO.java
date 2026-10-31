package com.cardmall.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "会员端首页：门店信息 + 在售卡列表")
public class AppHomeVO {

    @Schema(description = "门店信息")
    private StoreVO store;

    @Schema(description = "在售卡产品列表")
    private List<CardProductVO> products;
}
