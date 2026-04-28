import { apiClient } from './client';
import type { Flight } from '../types/flight';

export interface SearchParams {
  departure: string;
  arrival: string;
  date: string;          // YYYY-MM-DD
  seatClass?: string;
  passengers?: number;
}

export const flightsApi = {
  search: (params: SearchParams) =>
    apiClient.get<Flight[]>('/flights/search', { params }),
  detail: (flightId: number) =>
    apiClient.get<Flight>(`/flights/${flightId}`),
};
