import apiClient from './client';

export const chatApi = {
  ask: (bookingNumber: string, message: string) =>
    apiClient.post<{ reply: string }>(`/chat/booking/${bookingNumber}`, { message }),
};
