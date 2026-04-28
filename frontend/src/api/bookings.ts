import { apiClient } from './client';
import type { Booking, CreateBookingPayload } from '../types/booking';

export const bookingsApi = {
  create: (data: CreateBookingPayload) => apiClient.post<Booking>('/bookings', data),
  myBookings: () => apiClient.get<Booking[]>('/bookings'),
  detail: (bookingNumber: string) => apiClient.get<Booking>(`/bookings/${bookingNumber}`),
  cancel: (bookingNumber: string) => apiClient.post<Booking>(`/bookings/${bookingNumber}/cancel`),
};
