package com.navio.config;

import com.navio.domain.booking.Booking;
import com.navio.domain.booking.BookingRepository;
import com.navio.domain.booking.BookingStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RefundScheduler
 *
 * CANCEL_REQUESTED 상태인 예약을 자동으로 REFUNDED 처리하는 스케줄러.
 *
 * 동작:
 *   1분마다 실행 → CANCEL_REQUESTED 예약 조회
 *   → booking.startRefund() → booking.refund() (환불 완료)
 *
 * 실제 운영에서는 TossPayments 취소 API 호출 후 환불 확인을 받아야 한다.
 * Phase 2에서는 자동 처리로 단순화.
 *
 * 관련: Booking, BookingStatus, BookingRepository
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefundScheduler {

    private final BookingRepository bookingRepository;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void processRefunds() {
        List<Booking> toRefund = bookingRepository.findByStatus(BookingStatus.CANCEL_REQUESTED);
        if (toRefund.isEmpty()) return;

        log.info("[RefundScheduler] Processing {} refund requests", toRefund.size());
        for (Booking booking : toRefund) {
            booking.startRefund();
            booking.refund();
            log.info("[RefundScheduler] Refunded bookingId={}", booking.getId());
        }
    }
}
