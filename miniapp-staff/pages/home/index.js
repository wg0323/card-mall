/**
 * 首页：门店名 + 今日核销 + 扫码核销，次要入口走手动输入码与会员查询
 * 作者：card-mall 创建日期：2026-10-31
 */
import { getHome } from '../../api/staffApi';
import { getProfile, requireLogin, saveProfile } from '../../utils/auth';

Page({
  data: {
    storeName: '',
    todayVerifyCount: 0,
    scanning: false,
  },

  onShow() {
    if (!requireLogin()) {
      return;
    }
    // 自定义 tabBar 需要页面自己同步选中项
    const tabBar = typeof this.getTabBar === 'function' ? this.getTabBar() : null;
    if (tabBar) {
      tabBar.setData({ value: '/pages/home/index' });
    }
    this.setData({ storeName: getProfile().storeName ?? '' });
    this.loadHome();
  },

  onPullDownRefresh() {
    this.loadHome().finally(() => wx.stopPullDownRefresh());
  },

  async loadHome() {
    try {
      const home = await getHome();
      this.setData({ storeName: home.storeName, todayVerifyCount: home.todayVerifyCount ?? 0 });
      saveProfile({ storeName: home.storeName });
    } catch (error) {
      // 请求层已提示，卡片保留上一次的数据
    }
  },

  async scan() {
    if (this.data.scanning) {
      return;
    }
    this.setData({ scanning: true });
    try {
      const result = await new Promise((resolve, reject) => {
        wx.scanCode({ onlyFromCamera: true, scanType: ['qrCode'], success: resolve, fail: reject });
      });
      this.openConfirm(result.result);
    } catch (error) {
      // 取消扫码不算失败；相机未授权与游客 AppID 不支持要给不同的引导
      const message = String(error?.errMsg ?? '');
      if (message.includes('cancel')) {
        return;
      }
      if (message.includes('auth') || message.includes('permission')) {
        this.askCameraSetting();
        return;
      }
      wx.showToast({ title: '扫码不可用，请用下方手动输入码', icon: 'none' });
    } finally {
      this.setData({ scanning: false });
    }
  },

  /** 相机授权被拒后引导去设置页，同时保留手动输入码这条退路 */
  askCameraSetting() {
    wx.showModal({
      title: '需要相机权限',
      content: '扫码核销需要相机权限，可在设置页开启；也可以用「手动输入码」继续。',
      confirmText: '去设置',
      cancelText: '手动输码',
      success: (res) => {
        if (res.confirm) {
          wx.openSetting({
            fail: () => wx.showToast({ title: '打开设置失败，请用下方手动输入码', icon: 'none' }),
          });
        } else if (res.cancel) {
          this.manualInput();
        }
      },
    });
  },

  manualInput() {
    wx.showModal({
      title: '手动输入核销码',
      editable: true,
      placeholderText: '输入顾客出示的数字码',
      success: (res) => {
        if (res.confirm) {
          this.openConfirm(res.content);
        }
      },
    });
  },

  openConfirm(token) {
    const value = String(token ?? '').trim();
    if (!value) {
      wx.showToast({ title: '没有读到核销码', icon: 'none' });
      return;
    }
    wx.navigateTo({ url: `/pages/verify-confirm/index?token=${value}` });
  },

  goMemberSearch() {
    wx.navigateTo({ url: '/pages/member-search/index' });
  },
});
