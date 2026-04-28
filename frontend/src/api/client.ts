import axios from 'axios';
import { useAuthStore } from '../hooks/useAuth';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  headers: { 'Content-Type': 'application/json' },
});

apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().accessToken;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

let isRefreshing = false;
let failedQueue: Array<{ resolve: (v: string) => void; reject: (e: unknown) => void }> = [];

const processQueue = (error: unknown, token: string | null) => {
  failedQueue.forEach((p) => (error ? p.reject(error) : p.resolve(token!)));
  failedQueue = [];
};

apiClient.interceptors.response.use(
  (response) => {
    if (response.data?.success === true) return { ...response, data: response.data.data };
    return response;
  },
  async (error) => {
    const originalRequest = error.config;
    const errCode = error.response?.data?.error?.code;

    if (error.response?.status === 401 && errCode === 'TOKEN_EXPIRED' && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        }).then((token) => {
          originalRequest.headers.Authorization = `Bearer ${token}`;
          return apiClient(originalRequest);
        });
      }

      originalRequest._retry = true;
      isRefreshing = true;
      const refreshToken = useAuthStore.getState().refreshToken;

      if (!refreshToken) {
        useAuthStore.getState().clear();
        return Promise.reject(error);
      }

      try {
        const res = await apiClient.post('/auth/refresh', { refreshToken });
        const { accessToken, refreshToken: newRefresh } = res.data as {
          accessToken: string;
          refreshToken: string;
          userId: number;
          email: string;
          name: string;
        };
        const store = useAuthStore.getState();
        store.setAuth(store.user!, accessToken, newRefresh);
        processQueue(null, accessToken);
        originalRequest.headers.Authorization = `Bearer ${accessToken}`;
        return apiClient(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);
        useAuthStore.getState().clear();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    const errBody = error.response?.data?.error;
    if (errBody) error.message = `[${errBody.code}] ${errBody.message}`;
    return Promise.reject(error);
  },
);
