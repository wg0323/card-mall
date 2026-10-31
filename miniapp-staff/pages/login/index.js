/**
 * 登录：手机号 + 密码，成功后回首页；店员端不做静默登录
 * 作者：card-mall 创建日期：2026-10-31
 */
import { login } from '../../api/staffApi';
import { saveLogin } from '../../utils/auth';
import { BRAND_NAME } from '../../config/index';

Page({
  data: {
    brandName: BRAND_NAME,
    phone: '',
    password: '',
    submitting: false,
  },

  onLoad() {
    wx.setNavigationBarTitle({ title: '店员登录' });
  },

  onInput(event) {
    const { field } = event.currentTarget.dataset;
    this.setData({ [field]: event.detail.value });
  },

  async submit() {
    const { phone, password, submitting } = this.data;
    if (submitting) {
      return;
    }
    if (!phone || !password) {
      wx.showToast({ title: '请输入手机号和密码', icon: 'none' });
      return;
    }
    this.setData({ submitting: true });
    try {
      const result = await login(phone.trim(), password);
      saveLogin(result);
      wx.reLaunch({ url: '/pages/home/index' });
    } catch (error) {
      // 请求层已提示后端返回的 message
    } finally {
      this.setData({ submitting: false });
    }
  },
});
