/**
 * 我的：登录人姓名、门店、角色、头像，退出登录清本地态
 * 作者：card-mall 创建日期：2026-10-31
 */
import { clearLogin, getProfile, saveProfile } from '../../utils/auth';
import { roleLabel } from '../../utils/dict';
import { getMe, updateMyAvatar, uploadAvatar } from '../../api/staffApi';

Page({
  data: {
    name: '',
    storeName: '',
    roleText: '',
    avatar: '',
    uploading: false,
  },

  onShow() {
    const tabBar = typeof this.getTabBar === 'function' ? this.getTabBar() : null;
    if (tabBar) {
      tabBar.setData({ value: '/pages/profile/index' });
    }
    const profile = getProfile();
    this.setData({
      name: profile.name || '店员',
      storeName: profile.storeName || '',
      roleText: roleLabel(profile.role),
      avatar: profile.avatar || '',
    });
    this.loadMe();
  },

  // 本地缓存保证先有内容可看，姓名/角色/头像再以服务端为准
  async loadMe() {
    try {
      const me = await getMe();
      if (!me) {
        return;
      }
      saveProfile({ id: me.id, name: me.name, role: me.role, avatar: me.avatar });
      this.setData({
        name: me.name || '店员',
        roleText: roleLabel(me.role),
        avatar: me.avatar || '',
      });
    } catch (error) {
      // 请求层已提示；令牌失效时已回登录页
    }
  },

  // 头像由本人选：微信头像昵称能力要求用 open-type=chooseAvatar，选完即时生效
  async onChooseAvatar(event) {
    const avatarUrl = event.detail?.avatarUrl;
    if (!avatarUrl || this.data.uploading) {
      return;
    }
    this.setData({ uploading: true });
    try {
      const avatar = await uploadAvatar(avatarUrl);
      await updateMyAvatar(avatar);
      saveProfile({ avatar });
      this.setData({ avatar });
      wx.showToast({ title: '头像已更新', icon: 'success' });
    } catch (error) {
      // 请求层已提示
    } finally {
      this.setData({ uploading: false });
    }
  },

  logout() {
    wx.showModal({
      title: '退出登录',
      content: '退出后需要重新输入手机号和密码',
      success: (res) => {
        if (!res.confirm) {
          return;
        }
        clearLogin();
        wx.reLaunch({ url: '/pages/login/index' });
      },
    });
  },
});
