import { apiClient } from './client';

export interface ConfirmPaymentPayload {
  paymentKey: string;
  orderId: string;
  amount: number;
}

export const paymentsApi = {
  confirm: (data: ConfirmPaymentPayload) =>
    apiClient.post('/payments/confirm', data),
  status: (orderId: string) =>
    apiClient.get(`/payments/${orderId}`),
};
