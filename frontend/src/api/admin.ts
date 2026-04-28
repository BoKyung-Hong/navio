import { apiClient } from './client';
import type { Flight } from '../types/flight';
import type { Booking } from '../types/booking';

export interface FlightCreatePayload {
  flightNumber: string;
  airline: string;
  departureAirport: string;
  arrivalAirport: string;
  departureTime: string;
  arrivalTime: string;
  aircraftType: string;
  checkinOpenMinutes: number;
  checkinCloseMinutes: number;
  baggageCarryOnKg: number;
  baggageCarryOnSize: string;
  baggageCheckedKg: number;
  economySeats: number;
  economyPrice: number;
  businessSeats: number;
  businessPrice: number;
}

export const adminApi = {
  flights: () => apiClient.get<Flight[]>('/admin/flights'),
  createFlight: (data: FlightCreatePayload) => apiClient.post<Flight>('/admin/flights', data),
  updateFlight: (id: number, data: Partial<FlightCreatePayload>) =>
    apiClient.put<Flight>(`/admin/flights/${id}`, data),
  deleteFlight: (id: number) => apiClient.delete(`/admin/flights/${id}`),

  allBookings: (status?: string) =>
    apiClient.get<Booking[]>('/admin/bookings', { params: status ? { status } : undefined }),
  updateBookingStatus: (bookingNumber: string, status: string) =>
    apiClient.patch<Booking>(`/admin/bookings/${bookingNumber}/status`, { status }),

  users: () => apiClient.get<AdminUser[]>('/admin/users'),
  updateUserRole: (id: number, role: string) =>
    apiClient.patch<void>(`/admin/users/${id}/role`, { role }),
};

export interface AdminUser {
  id: number;
  email: string;
  name: string;
  phone: string;
  role: string;
  createdAt: string;
}
