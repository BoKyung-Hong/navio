package com.navio.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConfirmPaymentRequest(
        @NotBlank String paymentKey,
        @NotBlank String orderId,
        @NotNull Integer amount
) {}
