/**
 * 会员端认证接口
 * 作者：card-mall 创建日期：2026-10-30
 */
import { request } from '../utils/request';

export const login = (code) => request({ url: '/app/auth/login', method: 'POST', data: { code }, needAuth: false });

// 入参是 wx.getPhoneNumber 回调里的 code，后端解密后绑定到当前会员
export const bindPhone = (code) => request({ url: '/app/auth/phone', method: 'POST', data: { code } });
