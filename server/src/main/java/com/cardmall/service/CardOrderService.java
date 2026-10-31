package com.cardmall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cardmall.common.PageResult;
import com.cardmall.entity.CardOrder;
import com.cardmall.vo.CardOrderDetailVO;
import com.cardmall.vo.CardOrderVO;

import java.util.List;

public interface CardOrderService extends IService<CardOrder> {

    String create(Long memberId, Long productId);

    /** M1 模拟支付：已支付直接返回，状态 CAS 做幂等闸门 */
    void mockPay(Long memberId, String orderNo);

    CardOrder getByOrderNo(String orderNo);

    CardOrder getByOrderNoForMember(Long memberId, String orderNo);

    PageResult<CardOrder> pageByMember(Long memberId, int pageNum, int pageSize);

    List<CardOrder> listByMember(Long memberId);

    PageResult<CardOrder> pageByStatus(Integer payStatus, int pageNum, int pageSize);

    /** 后台订单列表：补齐会员昵称与手机号 */
    PageResult<CardOrderVO> pageForAdmin(Integer payStatus, int pageNum, int pageSize);

    /** 后台订单详情：订单 + 支付流水 */
    CardOrderDetailVO getDetailForAdmin(String orderNo);
}
