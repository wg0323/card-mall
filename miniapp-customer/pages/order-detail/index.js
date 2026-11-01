/**
 * 订单详情：待支付可继续支付
 * 作者：card-mall 创建日期：2026-10-30
 */
import { getCommentEligibility } from '../../api/commentApi';
import { getOrder, mockPay } from '../../api/orderApi';
import { decorateOrder } from '../../utils/dict';

Page({
  data: {
    order: null,
    paying: false,
    canComment: false,
    commentLoading: true,
  },

  onLoad(options) {
    this.orderNo = options.orderNo;
    this.loadData();
  },

  onShow() {
    // 从写评论页返回时刷新订单与可评状态
    if (this.initialized) {
      this.loadData();
    }
  },

  async loadData() {
    try {
      const order = await getOrder(this.orderNo);
      this.setData({ order: decorateOrder(order) });
      this.initialized = true;
      if (order.payStatus === 1) {
        this.loadCommentEligibility(order);
      } else {
        this.setData({ canComment: false, commentLoading: false });
      }
    } catch (error) {
      // 请求层已提示
    }
  },

  async loadCommentEligibility(order) {
    this.setData({ commentLoading: true });
    try {
      const canComment = await getCommentEligibility(order.productId, order.orderNo);
      this.setData({ canComment: Boolean(canComment), commentLoading: false });
    } catch (error) {
      this.setData({ canComment: false, commentLoading: false });
    }
  },

  async continuePay() {
    if (this.data.paying) {
      return;
    }
    this.setData({ paying: true });
    try {
      await mockPay(this.orderNo);
      wx.showToast({ title: '支付成功', icon: 'success' });
      this.loadData();
    } catch (error) {
      // 请求层已提示
    } finally {
      this.setData({ paying: false });
    }
  },

  goComment() {
    const { order, canComment, commentLoading } = this.data;
    if (!order || commentLoading) {
      return;
    }
    if (!canComment) {
      wx.navigateTo({ url: '/pages/my-comments/index' });
      return;
    }
    wx.navigateTo({
      url: `/pages/comment-edit/index?productId=${order.productId}&orderNo=${order.orderNo}`,
    });
  },

  goCards() {
    wx.switchTab({ url: '/pages/cards/index' });
  },
});
