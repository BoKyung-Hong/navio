export type BookingStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'TICKETED'
  | 'CANCEL_REQUESTED'
  | 'CANCELLED'
  | 'REFUND_PENDING'
  | 'REFUNDED';

export interface Booking {
  bookingId: number;
  bookingNumber: string;
  status: BookingStatus;
  flightId: number;
  seatClass: string;
  passengerCount: number;
  totalPrice: number;
  expiresAt: string;
  createdAt: string;
  passengers?: PassengerInfo[];
}

export interface CreateBookingPayload {
  flightId: number;
  seatClass: string;
  passengers: PassengerInput[];
}

export interface PassengerInfo {
  id: number;
  nameEnglish: string;
  nameKorean?: string;
  birthDate: string;
  gender: string;
  passportNumber?: string;
  nationality?: string;
}

export interface PassengerInput {
  nameKorean?: string;
  nameEnglish: string;
  birthDate: string;       // YYYY-MM-DD
  gender: 'MALE' | 'FEMALE';
  passportNumber?: string;
  nationality?: string;
}
