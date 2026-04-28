import { apiClient } from './client';

export interface SignupPayload {
  email: string;
  password: string;
  name: string;
  phone?: string;
}

export interface LoginPayload {
  email: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  userId: number;
  email: string;
  name: string;
  role: string;
  phone?: string;
}

export const authApi = {
  signup: (data: SignupPayload) => apiClient.post<number>('/auth/signup', data),
  login: (data: LoginPayload) => apiClient.post<TokenResponse>('/auth/login', data),
  refresh: (refreshToken: string) => apiClient.post<TokenResponse>('/auth/refresh', { refreshToken }),
  logout: () => apiClient.post<void>('/auth/logout'),
  me: () => apiClient.get<{ id: number; email: string; name: string; phone: string; role: string }>('/users/me'),
  updateMe: (data: { name?: string; phone?: string }) =>
    apiClient.patch<{ id: number; email: string; name: string; phone: string }>('/users/me', data),
  changePassword: (data: { currentPassword: string; newPassword: string }) =>
    apiClient.patch<void>('/users/me/password', data),
  deleteAccount: (password: string) =>
    apiClient.delete<void>('/users/me', { data: { password } }),
};
