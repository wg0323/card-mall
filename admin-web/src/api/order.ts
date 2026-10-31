import { http } from '@/api/request';
import type { CardOrderDetailVO, CardOrderVO, PageResult, VerifyRecordVO } from '@/types/api';

export function pageOrders(
  payStatus: number | undefined,
  pageNum: number,
  pageSize: number,
): Promise<PageResult<CardOrderVO>> {
  return http.get<PageResult<CardOrderVO>>('/admin/order/page', { payStatus, pageNum, pageSize });
}

export function getOrder(orderNo: string): Promise<CardOrderDetailVO> {
  return http.get<CardOrderDetailVO>(`/admin/order/${orderNo}`);
}

// 后台核销分页支持状态 + 日期区间，与店员端同一套查询
export function pageVerifies(
  status: number | undefined,
  startDate: string | undefined,
  endDate: string | undefined,
  pageNum: number,
  pageSize: number,
): Promise<PageResult<VerifyRecordVO>> {
  return http.get<PageResult<VerifyRecordVO>>('/admin/verify/page', {
    status,
    startDate,
    endDate,
    pageNum,
    pageSize,
  });
}
