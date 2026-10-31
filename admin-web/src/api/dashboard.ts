import { http } from '@/api/request';
import type { DashboardVO } from '@/types/api';

export function loadDashboard(): Promise<DashboardVO> {
  return http.get<DashboardVO>('/admin/dashboard');
}
