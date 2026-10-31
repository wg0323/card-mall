/**
 * 订单接口
 * 作者：card-mall 创建日期：2026-10-30
 */
import { request } from '../utils/request';

export const createOrder = (productId) => request({ url: '/app/order', method: 'POST', data: { productId } });

// M1 专用，M2 接真实支付后删除
export const mockPay = (orderNo) => request({ url: `/app/order/${orderNo}/mock-pay`, method: 'POST' });

export const pageOrders = (pageNum, pageSize) => request({ url: '/app/order/page', data: { pageNum, pageSize } });

export const getOrder = (orderNo) => request({ url: `/app/order/${orderNo}` });
