/**
 * 会员资料接口
 * 作者：card-mall 创建日期：2026-10-30
 */
import { request, uploadFile } from '../utils/request';

export const getProfile = () => request({ url: '/app/member/profile' });

// 上传头像，返回可访问的图片地址
export const uploadAvatar = (filePath) => uploadFile({ url: '/app/file', filePath });

// 头像地址传图片 URL 即可，后端会归一化成 objectKey 再入库
export const updateProfile = (data) => request({ url: '/app/member/profile', method: 'PUT', data });
