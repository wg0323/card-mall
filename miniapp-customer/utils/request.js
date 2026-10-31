/**
 * 请求封装：注入 token、剥 { code, message, data } 外壳、统一失败提示
 * 作者：card-mall 创建日期：2026-10-30
 */
import { BASE_URL } from '../config/index';

const TOKEN_KEY = 'card-mall-token';
const REFRESH_TOKEN_KEY = 'card-mall-refresh-token';
const PHONE_AUTH_KEY = 'card-mall-phone-auth-required';
const TOKEN_EXPIRED = 1002;
const REFRESH_URL = '/app/auth/refresh';

export const getToken = () => wx.getStorageSync(TOKEN_KEY) ?? '';

export const setToken = (token) => {
  wx.setStorageSync(TOKEN_KEY, token);
};

export const clearToken = () => {
  wx.removeStorageSync(TOKEN_KEY);
};

export const setRefreshToken = (token) => {
  wx.setStorageSync(REFRESH_TOKEN_KEY, token);
};

export const clearRefreshToken = () => {
  wx.removeStorageSync(REFRESH_TOKEN_KEY);
};

// refresh 也过期相当于一次彻底登出：留个标记，回首页后由 auth-gate 强制重新授权手机号
export const isPhoneAuthRequired = () => Boolean(wx.getStorageSync(PHONE_AUTH_KEY));

export const clearPhoneAuthRequired = () => {
  wx.removeStorageSync(PHONE_AUTH_KEY);
};

// 并发请求同时过期时只刷新一次，其余请求等这一个结果
let refreshing = null;

// wx.request 在部分基础库/开发者工具里返回 RequestTask 而不是 Promise，必须显式包一层
const send = ({ url, method = 'GET', data, needAuth = true }) => {
  const header = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (needAuth && token) {
    header.Authorization = `Bearer ${token}`;
  }

  return new Promise((resolve, reject) => {
    wx.request({
      url: `${BASE_URL}${url}`,
      method,
      data,
      header,
      success: resolve,
      fail: (error) => {
        console.error('[api] 网络失败', method, url, error?.errMsg ?? error);
        reject(error);
      },
    });
  });
};

const refreshToken = () => {
  if (!refreshing) {
    refreshing = (async () => {
      const token = wx.getStorageSync(REFRESH_TOKEN_KEY) ?? '';
      if (!token) {
        throw new Error('登录已过期');
      }
      const res = await send({
        url: REFRESH_URL,
        method: 'POST',
        data: { refreshToken: token },
        needAuth: false,
      });
      const body = res.data ?? {};
      if (body.code !== 0) {
        throw new Error(body.message || '登录已过期');
      }
      setToken(body.data.token);
      setRefreshToken(body.data.refreshToken);
    })().finally(() => {
      refreshing = null;
    });
  }
  return refreshing;
};

// 刷新失败说明 12 小时也过了：清登录态并标记需要重新授权手机号，回首页让 auth-gate 拦住
const resetLogin = (error) => {
  clearToken();
  clearRefreshToken();
  wx.setStorageSync(PHONE_AUTH_KEY, '1');
  wx.showToast({ title: '登录已过期，请重新授权手机号', icon: 'none' });
  wx.reLaunch({ url: '/pages/index/index' });
  return error;
};

export const request = async (options) => {
  let res = await send(options);
  let body = res.data ?? {};
  if (body.code === TOKEN_EXPIRED) {
    try {
      await refreshToken();
      res = await send(options);
      body = res.data ?? {};
    } catch (error) {
      throw resetLogin(error);
    }
  }

  if (body.code === 0) {
    return body.data;
  }
  if (body.code === 1001) {
    clearToken();
  }
  console.error('[api] 业务失败', options.method ?? 'GET', options.url, body.code, body.message);
  wx.showToast({ title: body.message || '请求失败', icon: 'none' });
  throw new Error(body.message || '请求失败');
};

const upload = ({ url, filePath }) => {
  const header = {};
  const token = getToken();
  if (token) {
    header.Authorization = `Bearer ${token}`;
  }

  // 不手动设 Content-Type：让 wx.uploadFile 自己生成 multipart boundary
  return new Promise((resolve, reject) => {
    wx.uploadFile({
      url: `${BASE_URL}${url}`,
      filePath,
      name: 'file',
      header,
      success: resolve,
      fail: (error) => {
        console.error('[api] 上传失败', url, error?.errMsg ?? error);
        reject(error);
      },
    });
  });
};

const parseBody = (raw) => {
  try {
    return JSON.parse(raw ?? '{}');
  } catch (error) {
    return {};
  }
};

// wx.uploadFile 不走 wx.request，单独包一层，token 与错误处理与 request 保持一致
export const uploadFile = async ({ url, filePath }) => {
  let res = await upload({ url, filePath });
  let body = parseBody(res.data);
  if (body.code === TOKEN_EXPIRED) {
    try {
      await refreshToken();
      res = await upload({ url, filePath });
      body = parseBody(res.data);
    } catch (error) {
      throw resetLogin(error);
    }
  }

  if (body.code === 0) {
    return body.data;
  }
  if (body.code === 1001) {
    clearToken();
  }
  console.error('[api] 上传失败', url, body.code, body.message);
  wx.showToast({ title: body.message || '上传失败', icon: 'none' });
  throw new Error(body.message || '上传失败');
};
