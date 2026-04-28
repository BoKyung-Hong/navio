package com.navio.domain.booking.dto;

import com.navio.domain.booking.Booking;
import com.navio.domain.booking.Passenger;

import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        Long bookingId,
        String bookingNumber,
        String status,
        Long flightId,
        String seatClass,
        Integer passengerCount,
        Integer totalPrice,
        LocalDateTime expiresAt,
        LocalDateTime createdAt,
        List<PassengerInfo> passengers
) {
    public record PassengerInfo(
            Long id,
            String nameEnglish,
            String nameKorean,
            String birthDate,
            String gender,
            String passportNumber,
            String nationality
    ) {
        static PassengerInfo from(Passenger p) {
            return new PassengerInfo(
                    p.getId(),
                    p.getNameEnglish(),
                    p.getNameKorean(),
                    p.getBirthDate() != null ? p.getBirthDate().toString() : null,
                    p.getGender(),
                    p.getPassportNumber(),
                    p.getNationality()
            );
        }
    }

    public static BookingResponse from(Booking b) {
        List<PassengerInfo> passengerInfos = b.getPassengers().stream()
                .map(PassengerInfo::from)
                .toList();
        return new BookingResponse(
                b.getId(),
                b.getBookingNumber(),
                b.getStatus().name(),
                b.getFlightId(),
                b.getSeatClass().name(),
                b.getPassengerCount(),
                b.getTotalPrice(),
                b.getCreatedAt().plusMinutes(10),
                b.getCreatedAt(),
                passengerInfos
        );
    }
}
