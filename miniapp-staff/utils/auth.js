/**
 * 登录态：token 与登录人信息（姓名/角色）都落本地，门店名由首页接口带回后补进来
 * 作者：card-mall 创建日期：2026-10-31
 */
import { clearToken, getToken, setRefreshToken, setToken } from './request';

const PROFILE_KEY = 'card-mall-staff-profile';

export const hasToken = () => Boolean(getToken());

export const saveLogin = ({ token, refreshToken, id, name, role }) => {
  setToken(token);
  setRefreshToken(refreshToken);
  wx.setStorageSync(PROFILE_KEY, { id, name, role });
};

export const getProfile = () => wx.getStorageSync(PROFILE_KEY) ?? {};

export const saveProfile = (patch) => {
  wx.setStorageSync(PROFILE_KEY, { ...getProfile(), ...patch });
};

export const clearLogin = () => {
  clearToken();
  wx.removeStorageSync(PROFILE_KEY);
};

// 页面 onShow 里兜一层：token 被清掉时不要停留在空页面上
export const requireLogin = () => {
  if (hasToken()) {
    return true;
  }
  wx.reLaunch({ url: '/pages/login/index' });
  return false;
};
