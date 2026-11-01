/**
 * 站内消息接口
 * 作者：card-mall 创建日期：2026-11-01
 */
import { request } from '../utils/request';

export const getUnreadCount = () => request({ url: '/app/message/unread-count' });

export const markMessageRead = (commentIds) =>
  request({ url: '/app/message/read', method: 'PUT', data: { commentIds } });
