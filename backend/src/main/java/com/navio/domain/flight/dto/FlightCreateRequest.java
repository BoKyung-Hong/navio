package com.navio.domain.flight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record FlightCreateRequest(
        @NotBlank String flightNumber,
        @NotBlank String airline,
        @NotBlank String departureAirport,
        @NotBlank String arrivalAirport,
        @NotNull LocalDateTime departureTime,
        @NotNull LocalDateTime arrivalTime,
        @NotBlank String aircraftType,
        int checkinOpenMinutes,
        int checkinCloseMinutes,
        int baggageCarryOnKg,
        String baggageCarryOnSize,
        int baggageCheckedKg,
        @Positive int economySeats,
        @Positive int economyPrice,
        @Positive int businessSeats,
        @Positive int businessPrice
) {}
