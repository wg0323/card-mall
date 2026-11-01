import axios, { type AxiosRequestConfig, type AxiosResponse } from 'axios';
import { ElMessage } from 'element-plus';
import router from '@/router';
import { useUserStore } from '@/stores/user';
import type { Result, TokenPair } from '@/types/api';

const instance = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

// 后端 1002 = access token 过期（去刷新）；3000 = 未登录或刷新也失效（回登录页）
const TOKEN_EXPIRED = 1002;
const UNAUTHORIZED = 3000;
const REFRESH_URL = '/api/admin/auth/refresh';

interface RetryableConfig extends AxiosRequestConfig {
  // 这条请求发出去时用的 access token，用来判断是否已被别的请求刷新过
  sentToken?: string;
  retried?: boolean;
}

// 并发请求同时过期时只刷新一次，其余请求等这一个结果
let refreshing: Promise<void> | null = null;

function refreshToken(): Promise<void> {
  const user = useUserStore();
  if (!user.refreshToken) {
    return Promise.reject(new Error('登录已过期'));
  }
  if (!refreshing) {
    // 用裸 axios 发刷新请求：走 instance 会被同一个拦截器再拦一次，失败时递归
    refreshing = axios
      .post<Result<TokenPair>>(REFRESH_URL, { refreshToken: user.refreshToken })
      .then(({ data }) => {
        if (data.code !== 0) {
          throw new Error(data.message || '登录已过期');
        }
        user.updateToken(data.data.token, data.data.refreshToken);
      })
      .finally(() => {
        refreshing = null;
      });
  }
  return refreshing;
}

async function ensureFreshToken(config: RetryableConfig): Promise<void> {
  const user = useUserStore();
  // 并发请求里晚到的那个拿到 1002 时，令牌可能已经被别的请求换过了，直接重放即可
  if (config.sentToken && config.sentToken !== user.token) {
    return;
  }
  await refreshToken();
}

instance.interceptors.request.use((config) => {
  const { token } = useUserStore();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  (config as RetryableConfig).sentToken = token;
  return config;
});

// 后端统一返回 { code, message, data }：code 为 0 时把 data 直接交给调用方，其余情况当场提示
instance.interceptors.response.use(
  async (response: AxiosResponse<Result<unknown>>) => {
    const body = response.data;
    if (body.code === 0) {
      return body.data as never;
    }
    if (body.code === TOKEN_EXPIRED) {
      const config = response.config as RetryableConfig;
      if (!config.retried) {
        config.retried = true;
        try {
          await ensureFreshToken(config);
          // 重放会再走一遍请求拦截器，自动带上新令牌
          return (await instance.request(config)) as never;
        } catch {
          useUserStore().clear();
          router.replace('/login');
          ElMessage.error('登录已过期，请重新登录');
          return Promise.reject(new Error('登录已过期'));
        }
      }
    }
    if (body.code === UNAUTHORIZED) {
      useUserStore().clear();
      router.replace('/login');
    }
    ElMessage.error(body.message || '请求失败');
    return Promise.reject(new Error(body.message || '请求失败'));
  },
  (error) => {
    ElMessage.error(error.message || '网络异常，请检查后端是否启动');
    return Promise.reject(error);
  },
);

// 拦截器已经把外壳剥掉，这里补回真实类型，避免每个调用点写一次 as
export const http = {
  get<T>(url: string, params?: Record<string, unknown>): Promise<T> {
    return instance.get(url, { params }) as unknown as Promise<T>;
  },
  post<T>(url: string, data?: unknown): Promise<T> {
    return instance.post(url, data) as unknown as Promise<T>;
  },
  put<T>(url: string, data?: unknown, params?: Record<string, unknown>): Promise<T> {
    return instance.put(url, data, { params }) as unknown as Promise<T>;
  },
  delete<T>(url: string): Promise<T> {
    return instance.delete(url) as unknown as Promise<T>;
  },
  // 图片上传单独放宽超时：5MB 图片走本地网络也可能超过默认的 10s
  upload<T>(url: string, file: File): Promise<T> {
    const form = new FormData();
    form.append('file', file);
    return instance.post(url, form, { timeout: 30000 }) as unknown as Promise<T>;
  },
};
