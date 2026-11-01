/**
 * 评论接口
 * 作者：card-mall 创建日期：2026-11-01
 */
import { request, uploadFile } from '../utils/request';

export const pageProductComments = (productId, pageNum, pageSize) =>
  request({ url: '/app/comment/page', data: { productId, pageNum, pageSize } });

export const getCommentEligibility = (productId, orderNo) => {
  const data = { productId };
  if (orderNo) {
    data.orderNo = orderNo;
  }
  return request({ url: '/app/comment/eligibility', data });
};

export const createComment = (data) => request({ url: '/app/comment', method: 'POST', data });

export const pageMyComments = (pageNum, pageSize) =>
  request({ url: '/app/comment/mine/page', data: { pageNum, pageSize } });

export const deleteComment = (id) => request({ url: `/app/comment/${id}`, method: 'DELETE' });

export const uploadCommentImage = (filePath) => uploadFile({ url: '/app/file', filePath });
