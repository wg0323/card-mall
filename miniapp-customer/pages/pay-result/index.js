/**
 * 支付结果：成功展示卡信息，失败可重新支付
 * 作者：card-mall 创建日期：2026-10-30
 */
import { mockPay } from '../../api/orderApi';

Page({
  data: {
    success: true,
    orderNo: '',
    cardName: '',
    amount: '',
    retrying: false,
  },

  onLoad(options) {
    this.setData({
      success: options.status === 'success',
      orderNo: options.orderNo ?? '',
      cardName: options.cardName ?? '',
      amount: options.amount ?? '',
    });
  },

  goCards() {
    wx.switchTab({ url: '/pages/cards/index' });
  },

  goHome() {
    wx.switchTab({ url: '/pages/index/index' });
  },

  async retryPay() {
    if (this.data.retrying) {
      return;
    }
    this.setData({ retrying: true });
    try {
      await mockPay(this.data.orderNo);
      this.setData({ success: true, retrying: false });
      wx.showToast({ title: '支付成功', icon: 'success' });
    } catch (error) {
      this.setData({ retrying: false });
    }
  },
});
