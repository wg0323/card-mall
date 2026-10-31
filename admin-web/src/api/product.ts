import { http } from '@/api/request';
import type { CardProductSaveRequest, CardProductVO, PageResult } from '@/types/api';

export function pageProducts(pageNum: number, pageSize: number): Promise<PageResult<CardProductVO>> {
  return http.get<PageResult<CardProductVO>>('/admin/product/page', { pageNum, pageSize });
}

export function createProduct(data: CardProductSaveRequest): Promise<number> {
  return http.post<number>('/admin/product', data);
}

export function updateProduct(id: number, data: CardProductSaveRequest): Promise<void> {
  return http.put<void>(`/admin/product/${id}`, data);
}

// 上架 1 / 下架 0 走查询参数，后端签名是 @RequestParam Integer status
export function updateProductStatus(id: number, status: number): Promise<void> {
  return http.put<void>(`/admin/product/${id}/status`, undefined, { status });
}
