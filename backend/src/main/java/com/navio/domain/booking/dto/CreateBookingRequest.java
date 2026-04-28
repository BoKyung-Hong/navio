package com.navio.domain.booking.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreateBookingRequest(
        @NotNull Long flightId,
        @NotBlank String seatClass,        // ECONOMY / BUSINESS / FIRST
        @NotEmpty @Valid List<PassengerInput> passengers
) {
    public record PassengerInput(
            String nameKorean,
            @NotBlank String nameEnglish,
            @NotNull LocalDate birthDate,
            @NotBlank String gender
    ) {}
}
