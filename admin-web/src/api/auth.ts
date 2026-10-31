import { http } from '@/api/request';
import type { ChangePasswordRequest, LoginRequest, LoginResult } from '@/types/api';

export function login(data: LoginRequest): Promise<LoginResult> {
  return http.post<LoginResult>('/admin/auth/login', data);
}

export function changePassword(data: ChangePasswordRequest): Promise<void> {
  return http.put<void>('/admin/auth/password', data);
}

// 退出时把刷新令牌一并作废，服务端只维护这一份会话
export function logout(refreshToken: string): Promise<void> {
  return http.post<void>('/admin/auth/logout', { refreshToken });
}
