import { http } from '@/api/request';

// bizType 与后端 FileBizType 白名单一致：card / store / staff
export type ImageBizType = 'card' | 'store' | 'staff';

// 返回可直接访问的图片地址，保存时后端会再归一化成 objectKey
export function uploadImage(file: File, bizType: ImageBizType): Promise<string> {
  return http.upload<string>(`/admin/file?bizType=${bizType}`, file);
}
