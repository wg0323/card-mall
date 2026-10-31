import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { LoginResult } from '@/types/api';

const TOKEN_KEY = 'card-mall-admin-token';
const REFRESH_TOKEN_KEY = 'card-mall-admin-refresh-token';
const NAME_KEY = 'card-mall-admin-name';

// 双令牌：access token 30 分钟过期，refresh token 12 小时，过期前用后者无感换新
export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem(TOKEN_KEY) ?? '');
  const refreshToken = ref(localStorage.getItem(REFRESH_TOKEN_KEY) ?? '');
  const name = ref(localStorage.getItem(NAME_KEY) ?? '');

  function setLogin(result: LoginResult): void {
    token.value = result.token;
    refreshToken.value = result.refreshToken ?? '';
    name.value = result.name ?? '';
    localStorage.setItem(TOKEN_KEY, token.value);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken.value);
    localStorage.setItem(NAME_KEY, name.value);
  }

  // 刷新只换令牌，姓名等登录人信息保持不变
  function updateToken(accessToken: string, newRefreshToken: string): void {
    token.value = accessToken;
    refreshToken.value = newRefreshToken;
    localStorage.setItem(TOKEN_KEY, token.value);
    localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken.value);
  }

  function clear(): void {
    token.value = '';
    refreshToken.value = '';
    name.value = '';
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    localStorage.removeItem(NAME_KEY);
  }

  return { token, refreshToken, name, setLogin, updateToken, clear };
});
