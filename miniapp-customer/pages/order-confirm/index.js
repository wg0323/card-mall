/**
 * 确认订单：校验协议与手机号后下单并 mock 支付
 * 作者：card-mall 创建日期：2026-10-30
 */
import { getProduct } from '../../api/homeApi';
import { createOrder, mockPay } from '../../api/orderApi';
import { getProfile } from '../../api/memberApi';
import { bindPhone } from '../../api/authApi';

Page({
  data: {
    product: null,
    member: null,
    agreed: false,
    submitting: false,
    binding: false,
  },

  onLoad(options) {
    this.productId = options.id;
    this.loadData();
  },

  async loadData() {
    try {
      const [product, member] = await Promise.all([getProduct(this.productId), getProfile()]);
      this.setData({ product, member });
    } catch (error) {
      // 请求层已提示
    }
  },

  toggleAgree() {
    this.setData({ agreed: !this.data.agreed });
  },

  // 手机号不能随登录暗默拿到，只能用户点一次授权；不阻断支付，只是方便店员后续按手机号查会员
  async onGetPhone(event) {
    const code = event.detail?.code;
    if (!code) {
      wx.showToast({ title: '未获取到手机号授权', icon: 'none' });
      return;
    }
    if (this.data.binding) {
      return;
    }
    this.setData({ binding: true });
    try {
      await bindPhone(code);
      const member = await getProfile();
      this.setData({ member });
      wx.showToast({ title: '已获取手机号', icon: 'success' });
    } catch (error) {
      // 请求层已提示
    } finally {
      this.setData({ binding: false });
    }
  },

  async pay() {
    const { agreed, member, product, submitting } = this.data;
    if (submitting) {
      return;
    }
    // 兜底：正常流程在 tab 页就被门禁拦住，这里防止异常状态绕过
    if (!member?.phone) {
      wx.showToast({ title: '请先授权手机号', icon: 'none' });
      return;
    }
    if (!agreed) {
      wx.showToast({ title: '请先阅读并同意购买须知', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });
    wx.showLoading({ title: '支付中', mask: true });
    try {
      const orderNo = await createOrder(product.id);
      await mockPay(orderNo);
      wx.hideLoading();
      wx.redirectTo({
        url: `/pages/pay-result/index?status=success&orderNo=${orderNo}&cardName=${product.name}&amount=${product.price}`,
      });
    } catch (error) {
      wx.hideLoading();
      this.setData({ submitting: false });
    }
  },
});
