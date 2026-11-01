import { http } from '@/api/request';
import type {
  AdminCommentVO,
  CommentReplyRequest,
  CommentStatusRequest,
  PageResult,
} from '@/types/api';

export function pageComments(params: {
  productId?: number;
  status?: number;
  pageNum: number;
  pageSize: number;
}): Promise<PageResult<AdminCommentVO>> {
  return http.get<PageResult<AdminCommentVO>>('/admin/comment/page', { ...params });
}

export function updateCommentStatus(id: number, status: number): Promise<void> {
  const body: CommentStatusRequest = { status };
  return http.put<void>(`/admin/comment/${id}/status`, body);
}

export function replyComment(id: number, replyContent: string): Promise<void> {
  const body: CommentReplyRequest = { replyContent };
  return http.put<void>(`/admin/comment/${id}/reply`, body);
}

export function deleteComment(id: number): Promise<void> {
  return http.delete<void>(`/admin/comment/${id}`);
}
