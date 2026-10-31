import { http } from '@/api/request';
import type { StoreSaveRequest, StoreVO } from '@/types/api';

export function getStore(): Promise<StoreVO> {
  return http.get<StoreVO>('/admin/store');
}

export function saveStore(data: StoreSaveRequest): Promise<void> {
  return http.put<void>('/admin/store', data);
}
