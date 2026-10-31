/**
 * 订单详情：待支付可继续支付
 * 作者：card-mall 创建日期：2026-10-30
 */
import { getOrder, mockPay } from '../../api/orderApi';
import { decorateOrder } from '../../utils/dict';

Page({
  data: {
    order: null,
    paying: false,
  },

  onLoad(options) {
    this.orderNo = options.orderNo;
    this.loadData();
  },

  async loadData() {
    try {
      const order = await getOrder(this.orderNo);
      this.setData({ order: decorateOrder(order) });
    } catch (error) {
      // 请求层已提示
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

  goCards() {
    wx.switchTab({ url: '/pages/cards/index' });
  },
});
