import { http } from '@/api/request';
import type { PageResult, StaffCreateRequest, StaffVO } from '@/types/api';

export function pageStaffs(pageNum: number, pageSize: number): Promise<PageResult<StaffVO>> {
  return http.get<PageResult<StaffVO>>('/admin/staff/page', { pageNum, pageSize });
}

// 后端不给初始密码入参，统一 123456，创建后要提示前端
export function createStaff(data: StaffCreateRequest): Promise<number> {
  return http.post<number>('/admin/staff', data);
}

export function updateStaffStatus(id: number, status: number): Promise<void> {
  return http.put<void>(`/admin/staff/${id}/status`, undefined, { status });
}

// 传图片地址即可，后端会归一化成 objectKey 再入库
export function updateStaffAvatar(id: number, avatar: string): Promise<void> {
  return http.put<void>(`/admin/staff/${id}/avatar`, { avatar });
}
