package com.navio.domain.booking.dto;

import com.navio.domain.booking.Booking;

import java.time.LocalDateTime;

public record BookingResponse(
        Long bookingId,
        String bookingNumber,
        String status,
        Long flightId,
        String seatClass,
        Integer passengerCount,
        Integer totalPrice,
        LocalDateTime expiresAt,
        LocalDateTime createdAt
) {
    public static BookingResponse from(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getBookingNumber(),
                b.getStatus().name(),
                b.getFlightId(),
                b.getSeatClass().name(),
                b.getPassengerCount(),
                b.getTotalPrice(),
                b.getCreatedAt().plusMinutes(10),
                b.getCreatedAt()
        );
    }
}
