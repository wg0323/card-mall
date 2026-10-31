import { http } from '@/api/request';
import type { AdminMemberVO, MemberDetailVO, PageResult } from '@/types/api';

export function pageMembers(
  keyword: string,
  pageNum: number,
  pageSize: number,
): Promise<PageResult<AdminMemberVO>> {
  return http.get<PageResult<AdminMemberVO>>('/admin/member/page', { keyword, pageNum, pageSize });
}

export function getMemberDetail(id: number): Promise<MemberDetailVO | null> {
  return http.get<MemberDetailVO | null>(`/admin/member/${id}`);
}
