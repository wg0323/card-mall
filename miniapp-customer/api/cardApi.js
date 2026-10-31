/**
 * 会员卡与核销码接口
 * 作者：card-mall 创建日期：2026-10-30
 */
import { request } from '../utils/request';

export const pageCards = (status, pageNum, pageSize) =>
  request({ url: '/app/card/page', data: { status, pageNum, pageSize } });

export const getCard = (id) => request({ url: `/app/card/${id}` });

// 返回 { token, expireIn }，token 一次性使用，60 秒失效
export const createVerifyCode = (id) => request({ url: `/app/card/${id}/verify-code`, method: 'POST' });
