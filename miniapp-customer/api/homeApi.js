/**
 * 门店与卡产品接口
 * 作者：card-mall 创建日期：2026-10-30
 */
import { request } from '../utils/request';

export const getHome = () => request({ url: '/app/home' });

export const getProduct = (id) => request({ url: `/app/product/${id}` });
