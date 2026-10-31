/**
 * 我的卡包：按状态分 Tab，进入详情看记录
 * 作者：card-mall 创建日期：2026-10-30
 */
import { pageCards } from '../../api/cardApi';
import { getProfile } from '../../api/memberApi';
import { hasToken } from '../../utils/auth';
import { clearPhoneAuthRequired, isPhoneAuthRequired } from '../../utils/request';
import { decorateCard } from '../../utils/dict';

const TABS = [
  { status: 1, label: '可用' },
  { status: 2, label: '已用完' },
  { status: 3, label: '已过期' },
];

Page({
  data: {
    tabs: TABS,
    activeIndex: 0,
    needPhone: false,
    list: [],
    loading: true,
  },

  onShow() {
    const tabBar = typeof this.getTabBar === 'function' ? this.getTabBar() : null;
    if (tabBar) {
      tabBar.setData({ value: '/pages/cards/index' });
    }
    this.checkPhone();
    // 从核销页返回时余次可能已变，每次显示都刷新
    this.loadData();
  },

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

  switchTab(event) {
    const { index } = event.currentTarget.dataset;
    if (index === this.data.activeIndex) {
      return;
    }
    this.setData({ activeIndex: index, list: [] });
    this.loadData();
  },

  async loadData() {
    const { activeIndex } = this.data;
    if (!hasToken()) {
      this.setData({ loading: false, needPhone: true });
      return;
    }
    this.setData({ loading: true });
    try {
      const page = await pageCards(TABS[activeIndex].status, 1, 50);
      this.setData({ list: (page.list ?? []).map(decorateCard) });
    } catch (error) {
      this.setData({ list: [] });
    } finally {
      this.setData({ loading: false });
    }
  },

  goDetail(event) {
    const { id } = event.currentTarget.dataset;
    wx.navigateTo({ url: `/pages/card-detail/index?id=${id}` });
  },

  goHome() {
    wx.switchTab({ url: '/pages/index/index' });
  },
});
