export interface Flight {
  id: number;
  flightNumber: string;
  airline: string;
  departure: Endpoint;
  arrival: Endpoint;
  aircraftType: string | null;
  seats: SeatInfo[];
  checkin: { openMinutesBefore: number; closeMinutesBefore: number };
}

export interface Endpoint {
  airport: string;
  airportName: string;
  time: string;            // ISO datetime
  timezone: string;
}

export interface SeatInfo {
  seatClass: 'ECONOMY' | 'BUSINESS' | 'FIRST';
  available: number;
  price: number;
}
