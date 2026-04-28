package com.navio.config;

import com.navio.domain.booking.Booking;
import com.navio.domain.booking.BookingRepository;
import com.navio.domain.booking.BookingStatus;
import com.navio.domain.payment.PaymentRepository;
import com.navio.domain.payment.TossPaymentsClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RefundScheduler
 *
 * 1) CANCEL_REQUESTED → 결제 미확인 취소건 자동 REFUNDED 처리 (1분 주기)
 * 2) REFUND_PENDING   → Toss 취소 API 재시도; 성공 시 REFUNDED, 실패 시 유지 (5분 주기)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefundScheduler {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final TossPaymentsClient tossPaymentsClient;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void processCancelRequested() {
        List<Booking> toRefund = bookingRepository.findByStatus(BookingStatus.CANCEL_REQUESTED);
        if (toRefund.isEmpty()) return;

        log.info("[RefundScheduler] Processing {} CANCEL_REQUESTED bookings", toRefund.size());
        for (Booking booking : toRefund) {
            booking.startRefund();
            booking.refund();
            log.info("[RefundScheduler] Auto-refunded bookingId={}", booking.getId());
        }
    }

    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void retryRefundPending() {
        List<Booking> pending = bookingRepository.findByStatus(BookingStatus.REFUND_PENDING);
        if (pending.isEmpty()) return;

        log.info("[RefundScheduler] Retrying {} REFUND_PENDING bookings", pending.size());
        for (Booking booking : pending) {
            paymentRepository.findByBookingId(booking.getId()).ifPresentOrElse(payment -> {
                try {
                    tossPaymentsClient.cancel(payment.getPaymentKey(), "환불 재시도");
                    payment.markCanceled();
                    booking.refund();
                    log.info("[RefundScheduler] Retry success bookingId={}", booking.getId());
                } catch (Exception e) {
                    log.warn("[RefundScheduler] Retry failed bookingId={}: {}", booking.getId(), e.getMessage());
                }
            }, () -> {
                // 결제 기록 없으면 그냥 환불 완료 처리
                booking.refund();
                log.info("[RefundScheduler] No payment, auto-refunded bookingId={}", booking.getId());
            });
        }
    }
}
