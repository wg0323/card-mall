package com.cardmall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cardmall.common.BizException;
import com.cardmall.common.ErrorCode;
import com.cardmall.common.PageResult;
import com.cardmall.converter.CardOrderConverter;
import com.cardmall.converter.PayTransactionConverter;
import com.cardmall.entity.CardOrder;
import com.cardmall.entity.CardProduct;
import com.cardmall.entity.MemberInfo;
import com.cardmall.entity.PayTransaction;
import com.cardmall.mapper.CardOrderMapper;
import com.cardmall.mapper.PayTransactionMapper;
import com.cardmall.pay.PayChannel;
import com.cardmall.pay.PrepayResult;
import com.cardmall.service.CardOrderService;
import com.cardmall.service.CardProductService;
import com.cardmall.service.MemberCardService;
import com.cardmall.service.MemberService;
import com.cardmall.service.StoreService;
import com.cardmall.vo.CardOrderDetailVO;
import com.cardmall.vo.CardOrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class CardOrderServiceImpl extends ServiceImpl<CardOrderMapper, CardOrder> implements CardOrderService {

    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final PayTransactionMapper payTransactionMapper;

    private final CardProductService cardProductService;

    private final MemberCardService memberCardService;

    private final StoreService storeService;

    private final PayChannel payChannel;

    private final MemberService memberService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String create(Long memberId, Long productId) {
        CardProduct product = cardProductService.getOnSaleById(productId);
        if (product == null) {
            throw new BizException(ErrorCode.CARD_NOT_USABLE, "卡已下架或不存在");
        }
        CardOrder order = new CardOrder();
        order.setOrderNo(generateOrderNo());
        order.setStoreId(storeService.getMainStore().getId());
        order.setMemberId(memberId);
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setAmount(product.getPrice());
        order.setPayChannel("");
        order.setPayStatus(CardOrder.STATUS_UNPAID);
        baseMapper.insert(order);
        return order.getOrderNo();
    }

    /**
     * M1 的模拟支付：订单已支付 → 写流水 → 发卡，同一事务。
     * 重复调用直接返回，不重复发卡（订单状态在这里就是幂等闸门）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void mockPay(Long memberId, String orderNo) {
        CardOrder order = getByOrderNo(orderNo);
        if (order == null || !order.getMemberId().equals(memberId)) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        if (order.getPayStatus() == CardOrder.STATUS_PAID) {
            return;
        }
        if (order.getPayStatus() != CardOrder.STATUS_UNPAID) {
            throw new BizException(ErrorCode.ORDER_STATUS_INVALID, "订单状态不允许支付");
        }

        PrepayResult pay = payChannel.prepay(orderNo, order.getAmount(), "");

        // 用状态 CAS 做唯一闸门：并发双击时只有一个事务能把 0 → 1
        int affected = baseMapper.update(null, new UpdateWrapper<CardOrder>()
                .eq("id", order.getId())
                .eq("pay_status", CardOrder.STATUS_UNPAID)
                .set("pay_status", CardOrder.STATUS_PAID)
                .set("pay_channel", pay.getChannel())
                .set("paid_time", LocalDateTime.now()));
        if (affected == 0) {
            return;
        }

        PayTransaction transaction = new PayTransaction();
        transaction.setOrderNo(orderNo);
        transaction.setChannel(pay.getChannel());
        transaction.setTransactionId(pay.getTransactionId());
        transaction.setAmount(order.getAmount());
        transaction.setStatus(1);
        transaction.setFinishTime(LocalDateTime.now());
        payTransactionMapper.insert(transaction);

        memberCardService.issueCard(order);
    }

    @Override
    public CardOrder getByOrderNo(String orderNo) {
        return baseMapper.selectOne(new QueryWrapper<CardOrder>().eq("order_no", orderNo));
    }

    @Override
    public CardOrder getByOrderNoForMember(Long memberId, String orderNo) {
        CardOrder order = getByOrderNo(orderNo);
        if (order == null || !order.getMemberId().equals(memberId)) {
            throw new BizException(ErrorCode.ORDER_NOT_FOUND, "订单不存在");
        }
        return order;
    }

    @Override
    public PageResult<CardOrder> pageByMember(Long memberId, int pageNum, int pageSize) {
        Page<CardOrder> result = baseMapper.selectPage(
                new Page<CardOrder>(pageNum, pageSize),
                new QueryWrapper<CardOrder>().eq("member_id", memberId).orderByDesc("id"));
        return PageResult.of(result.getRecords(), result.getTotal());
    }

    @Override
    public List<CardOrder> listByMember(Long memberId) {
        return baseMapper.selectList(new QueryWrapper<CardOrder>()
                .eq("member_id", memberId).orderByDesc("id"));
    }

    @Override
    public PageResult<CardOrder> pageByStatus(Integer payStatus, int pageNum, int pageSize) {
        QueryWrapper<CardOrder> qw = new QueryWrapper<CardOrder>().orderByDesc("id");
        if (payStatus != null) {
            qw.eq("pay_status", payStatus);
        }
        Page<CardOrder> result = baseMapper.selectPage(new Page<CardOrder>(pageNum, pageSize), qw);
        return PageResult.of(result.getRecords(), result.getTotal());
    }

    /**
     * 后台列表要显示会员，但 card_order 只存 member_id；
     * 按当前页的 id 批量查一次会员，不做 N+1。
     */
    @Override
    public PageResult<CardOrderVO> pageForAdmin(Integer payStatus, int pageNum, int pageSize) {
        PageResult<CardOrder> page = pageByStatus(payStatus, pageNum, pageSize);
        List<Long> memberIds = new ArrayList<Long>();
        for (CardOrder order : page.getList()) {
            memberIds.add(order.getMemberId());
        }
        Map<Long, MemberInfo> members = memberService.mapByIds(memberIds);
        return page.convert(order -> CardOrderConverter.toVO(order, members.get(order.getMemberId())));
    }

    @Override
    public CardOrderDetailVO getDetailForAdmin(String orderNo) {
        CardOrder order = getByOrderNo(orderNo);
        if (order == null) {
            return null;
        }
        CardOrderDetailVO vo = new CardOrderDetailVO();
        vo.setOrder(CardOrderConverter.toVO(order, memberService.getById(order.getMemberId())));
        vo.setPayTransactions(PayTransactionConverter.toVOList(payTransactionMapper.selectList(
                new QueryWrapper<PayTransaction>()
                        .select("id", "order_no", "channel", "transaction_id", "amount", "status",
                                "finish_time", "create_time")
                        .eq("order_no", orderNo)
                        .orderByDesc("id"))));
        return vo;
    }

    private String generateOrderNo() {
        return LocalDateTime.now().format(ORDER_NO_FORMATTER)
                + ThreadLocalRandom.current().nextInt(1000, 10000);
    }
}
