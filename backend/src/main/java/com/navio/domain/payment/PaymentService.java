package com.navio.domain.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.navio.common.BusinessException;
import com.navio.common.ErrorCode;
import com.navio.domain.booking.Booking;
import com.navio.domain.booking.BookingRepository;
import com.navio.domain.booking.BookingStatus;
import com.navio.domain.payment.dto.ConfirmPaymentRequest;
import com.navio.domain.seat.SeatInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * PaymentService
 *
 * TossPayments 결제 승인 처리 비즈니스 로직.
 *
 * confirm() 흐름:
 *   1. orderId(= bookingNumber)로 예약 조회 → PENDING 상태 확인
 *   2. 금액 검증: 요청 금액과 예약 총액 비교 (위변조 방지 1차 방어선)
 *   3. TossPayments 서버에 결제 승인 요청 (TossPaymentsClient.confirm)
 *   4. 응답 status가 "DONE"인지 확인
 *   5. Payment 엔티티 저장 (idempotent: 이미 존재 시 업데이트)
 *   6. Booking.confirm() → CONFIRMED 상태로 전환
 *
 * 전체가 하나의 트랜잭션으로 처리되며, Toss 서버 호출 실패 시 롤백된다.
 *
 * 관련: Payment, Booking, TossPaymentsClient, PaymentController
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    private final TossPaymentsClient tossPaymentsClient;

    @Transactional
    public Payment confirm(ConfirmPaymentRequest req) {
        Booking booking = bookingRepository.findByBookingNumber(req.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new BusinessException(ErrorCode.BOOKING_EXPIRED);
        }

        if (!booking.getTotalPrice().equals(req.amount())) {
            throw new BusinessException(ErrorCode.AMOUNT_MISMATCH);
        }

        JsonNode tossResponse = tossPaymentsClient.confirm(req.paymentKey(), req.orderId(), req.amount());
        String status = tossResponse.path("status").asText();
        String method = tossResponse.path("method").asText(null);

        if (!"DONE".equals(status)) {
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, "Toss status=" + status);
        }

        Payment payment = paymentRepository.findByBookingId(booking.getId())
                .orElseGet(() -> Payment.builder()
                        .bookingId(booking.getId())
                        .orderId(req.orderId())
                        .amount(req.amount())
                        .status(PaymentStatus.READY)
                        .build());
        payment.markDone(req.paymentKey(), method);
        Payment saved = paymentRepository.save(payment);

        booking.confirm();

        return saved;
    }
}
