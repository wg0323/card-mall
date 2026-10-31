/**
 * 首页：门店信息 + 在售卡双列
 * 作者：card-mall 创建日期：2026-10-30
 */
import { getHome } from '../../api/homeApi';
import { getProfile } from '../../api/memberApi';
import { hasToken } from '../../utils/auth';
import { clearPhoneAuthRequired, isPhoneAuthRequired } from '../../utils/request';
import { isBusinessOpen } from '../../utils/business';
import { BRAND_NAME } from '../../config/index';

Page({
  data: {
    loading: true,
    loadError: '',
    needPhone: false,
    store: null,
    isOpen: false,
    products: [],
  },

  onLoad() {
    wx.setNavigationBarTitle({ title: BRAND_NAME });
    this.loadData();
  },

  onShow() {
    // 自定义 tabBar 需要页面自己同步选中项
    const tabBar = typeof this.getTabBar === 'function' ? this.getTabBar() : null;
    if (tabBar) {
      tabBar.setData({ value: '/pages/index/index' });
    }
    // 停留期间可能跨过营业时间点，每次显示按当前时间重算
    if (this.data.store) {
      this.setData({ isOpen: isBusinessOpen(this.data.store.businessHours) });
    }
    this.checkPhone();
  },

  // 没有手机号就全屏拦住，不给绕过入口
  async checkPhone() {
    if (!hasToken()) {
      // 没有登录态就直接亮出授权门禁，不自动登录
      this.setData({ needPhone: true });
      return;
    }
    try {
      const member = await getProfile();
      // refresh 过期或未绑手机号都要重新授权
      this.setData({ needPhone: isPhoneAuthRequired() || !member?.phone });
    } catch (error) {
      // 请求层已提示
    }
  },

  onPhoneBound() {
    clearPhoneAuthRequired();
    this.setData({ needPhone: false });
    this.loadData();
  },

  onPullDownRefresh() {
    this.loadData().finally(() => wx.stopPullDownRefresh());
  },

  async loadData() {
    if (!hasToken()) {
      this.setData({ loading: false, needPhone: true });
      return;
    }
    this.setData({ loading: true, loadError: '' });
    try {
      const home = await getHome();
      this.setData({
        store: home.store,
        isOpen: isBusinessOpen(home.store?.businessHours),
        products: home.products ?? [],
      });
    } catch (error) {
      // 请求层已统一提示，这里把原因留在页面上，避免白屏无提示
      this.setData({ loadError: error?.errMsg ?? error?.message ?? '加载失败' });
    } finally {
      this.setData({ loading: false });
    }
  },

  goDetail(event) {
    const { id } = event.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/detail/index?id=${id}` });
  },

  callStore() {
    const { phone } = this.data.store ?? {};
    if (!phone) {
      wx.showToast({ title: '门店未留电话', icon: 'none' });
      return;
    }
    wx.makePhoneCall({ phoneNumber: phone });
  },

  openLocation() {
    const { store } = this.data;
    if (!store?.latitude || !store?.longitude) {
      wx.showToast({ title: '门店未设置坐标', icon: 'none' });
      return;
    }
    wx.openLocation({
      latitude: Number(store.latitude),
      longitude: Number(store.longitude),
      name: store.name,
      address: store.address,
    });
  },
});
