/**
 * 卡详情：展示卡信息并进入下单
 * 作者：card-mall 创建日期：2026-10-30
 */
import { getProduct } from '../../api/homeApi';

Page({
  data: {
    loading: true,
    product: null,
  },

  onLoad(options) {
    this.productId = options.id;
    this.loadData();
  },

  async loadData() {
    this.setData({ loading: true });
    try {
      const product = await getProduct(this.productId);
      this.setData({ product });
    } catch (error) {
      // 请求层已提示
    } finally {
      this.setData({ loading: false });
    }
  },

  buy() {
    const { product } = this.data;
    if (product?.status !== 1) {
      wx.showToast({ title: '该卡已售罄', icon: 'none' });
      return;
    }
    wx.navigateTo({ url: `/pages/order-confirm/index?id=${product.id}` });
  },
});
