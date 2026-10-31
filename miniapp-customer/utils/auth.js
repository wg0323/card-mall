/**
 * 登录态：用 wx.login 的 code 换 token，并发调用只发一次请求
 * 作者：card-mall 创建日期：2026-10-30
 */
import { login } from '../api/authApi';
import { getToken, setRefreshToken, setToken } from './request';

let pending = null;

export const hasToken = () => Boolean(getToken());

// 无登录态时回首页，由首页的授权门禁接管；页面里不自动登录
export const requireLogin = () => {
  if (getToken()) {
    return true;
  }
  wx.reLaunch({ url: '/pages/index/index' });
  return false;
};

// 同 wx.request：未传回调时不一定返回 Promise，这里显式包一层
const loginCode = () =>
  new Promise((resolve, reject) => {
    wx.login({
      success: (res) => resolve(res.code ?? ''),
      fail: (error) => {
        console.error('[auth] wx.login 失败', error?.errMsg ?? error);
        reject(error);
      },
    });
  });

export const ensureLogin = async (force = false) => {
  const cached = getToken();
  if (cached && !force) {
    return cached;
  }
  if (pending) {
    return pending;
  }

  pending = (async () => {
    const code = await loginCode();
    const result = await login(code);
    setToken(result.token);
    setRefreshToken(result.refreshToken);
    return result.token;
  })();

  try {
    return await pending;
  } finally {
    pending = null;
  }
};
