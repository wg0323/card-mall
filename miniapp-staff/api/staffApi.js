/**
 * 店员端接口：登录、首页、核销、核销记录、会员查询
 * 作者：card-mall 创建日期：2026-10-31
 */
import { request, uploadFile } from '../utils/request';

export const login = (phone, password) =>
  request({ url: '/staff/auth/login', method: 'POST', data: { phone, password }, needAuth: false });

export const getHome = () => request({ url: '/staff/home' });

// 预览不扣次不删码
export const previewVerify = (token) => request({ url: '/staff/verify/preview', method: 'POST', data: { token } });

export const confirmVerify = ({ token, requestId, times, remark }) =>
  request({ url: '/staff/verify/confirm', method: 'POST', data: { token, requestId, times, remark } });

// GET 的 query 里不能留空值，否则会被拼成字符串 "undefined"
export const pageRecords = (params) => {
  const data = Object.keys(params).reduce((acc, key) => {
    const value = params[key];
    if (value !== undefined && value !== null && value !== '') {
      acc[key] = value;
    }
    return acc;
  }, {});
  return request({ url: '/staff/verify/page', data });
};

export const searchMember = (phone) => request({ url: '/staff/member/search', data: { phone } });

// 会员详情：资料 + 持卡 + 订单，一次拿齐供详情页两个标签用
export const getMemberDetailByPhone = (phone) => request({ url: '/staff/member/detail', data: { phone } });

// 当前登录人：本地缓存可能过期，头像/姓名以服务端为准
export const getMe = () => request({ url: '/staff/me' });

export const uploadAvatar = (filePath) => uploadFile({ url: '/staff/file', filePath });

export const updateMyAvatar = (avatar) =>
  request({ url: '/staff/me/avatar', method: 'PUT', data: { avatar } });
