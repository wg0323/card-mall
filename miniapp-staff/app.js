/**
 * 应用入口：无登录态直接回登录页，店员端不做静默登录
 * 作者：card-mall 创建日期：2026-10-31
 */
import { hasToken } from './utils/auth';

App({
  onLaunch() {
    if (!hasToken()) {
      wx.reLaunch({ url: '/pages/login/index' });
    }
  },
});
