/**
 * 我的：会员资料、绑定手机号、常用入口
 * 作者：card-mall 创建日期：2026-10-30
 */
import { getProfile, updateProfile, uploadAvatar } from '../../api/memberApi';
import { getHome } from '../../api/homeApi';
import { getUnreadCount } from '../../api/messageApi';
import { hasToken } from '../../utils/auth';
import { clearPhoneAuthRequired, isPhoneAuthRequired } from '../../utils/request';
import { BRAND_NAME } from '../../config/index';

Page({
  data: {
    brandName: BRAND_NAME,
    member: null,
    avatarUploading: false,
    needPhone: false,
    commentNote: '',
  },

  onShow() {
    const tabBar = typeof this.getTabBar === 'function' ? this.getTabBar() : null;
    if (tabBar) {
      tabBar.setData({ value: '/pages/profile/index' });
    }
    wx.setNavigationBarTitle({ title: BRAND_NAME });
    this.loadData();
  },

  async loadData() {
    if (!hasToken()) {
      // 没有登录态就直接亮出授权门禁，不自动登录
      this.setData({ needPhone: true });
      return;
    }
    try {
      const member = await getProfile();
      // refresh 过期或未绑手机号都要重新授权
      this.setData({ member, needPhone: isPhoneAuthRequired() || !member?.phone });
      this.loadUnreadCount();
    } catch (error) {
      // 请求层已提示
    }
  },

  onPhoneBound() {
    clearPhoneAuthRequired();
    this.setData({ needPhone: false });
    this.loadData();
  },

  // 角标与入口文案共用一个请求结果，避免同一次 onShow 发两次
  async loadUnreadCount() {
    try {
      const count = await getUnreadCount();
      const unread = Number(count ?? 0);
      this.setData({ commentNote: unread > 0 ? `${unread} 条新回复` : '' });
      const tabBar = typeof this.getTabBar === 'function' ? this.getTabBar() : null;
      if (tabBar) {
        tabBar.refreshBadge(unread);
      }
    } catch (error) {
      // 请求层已提示
    }
  },

  // 头像只能由用户自己选：选完直接上传并保存，不再单独确认
  async onChooseAvatar(event) {
    const avatarUrl = event.detail?.avatarUrl;
    if (!avatarUrl || this.data.avatarUploading) {
      return;
    }
    this.setData({ avatarUploading: true });
    try {
      const avatar = await uploadAvatar(avatarUrl);
      await updateProfile({ avatar });
      this.setData({ 'member.avatar': avatar });
      wx.showToast({ title: '头像已更新', icon: 'success' });
    } catch (error) {
      // 请求层已提示
    } finally {
      this.setData({ avatarUploading: false });
    }
  },

  // 昵称没有保存按钮：失焦时与已保存的值比对，变了才提交
  async onNicknameBlur(event) {
    const nickname = event.detail.value.trim();
    if (!nickname || nickname === (this.data.member?.nickname ?? '').trim()) {
      return;
    }
    try {
      await updateProfile({ nickname });
      this.setData({ 'member.nickname': nickname });
    } catch (error) {
      // 请求层已提示
    }
  },

  goCards() {
    wx.switchTab({ url: '/pages/cards/index' });
  },

  goOrders() {
    wx.navigateTo({ url: '/pages/orders/index' });
  },

  goMyComments() {
    wx.navigateTo({ url: '/pages/my-comments/index' });
  },

  async callStore() {
    try {
      const home = await getHome();
      const phone = home?.store?.phone;
      if (!phone) {
        wx.showToast({ title: '门店未留电话', icon: 'none' });
        return;
      }
      wx.makePhoneCall({ phoneNumber: phone });
    } catch (error) {
      // 请求层已提示
    }
  },
});
