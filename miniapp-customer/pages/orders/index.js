/**
 * 我的订单：按时间倒序列表
 * 作者：card-mall 创建日期：2026-10-30
 */
import { pageOrders } from '../../api/orderApi';
import { requireLogin } from '../../utils/auth';
import { decorateOrder } from '../../utils/dict';

Page({
  data: {
    loading: true,
    list: [],
  },

  onShow() {
    if (!requireLogin()) {
      return;
    }
    this.loadData();
  },

  async loadData() {
    this.setData({ loading: true });
    try {
      const page = await pageOrders(1, 50);
      this.setData({ list: (page.list ?? []).map(decorateOrder) });
    } catch (error) {
      this.setData({ list: [] });
    } finally {
      this.setData({ loading: false });
    }
  },

  goDetail(event) {
    const { orderNo } = event.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/order-detail/index?orderNo=${orderNo}` });
  },

  goHome() {
    wx.switchTab({ url: '/pages/index/index' });
  },
});
