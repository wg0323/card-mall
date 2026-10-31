/**
 * 应用入口：不在启动时自动登录，登录与手机号授权都由用户在门禁页点击触发
 * 作者：card-mall 创建日期：2026-10-30
 */
import { BRAND_NAME } from './config/index';

App({
  globalData: {
    brandName: BRAND_NAME,
  },
});
