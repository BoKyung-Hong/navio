package com.navio.config;

import com.navio.domain.booking.Booking;
import com.navio.domain.booking.BookingRepository;
import com.navio.domain.seat.SeatInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * BookingExpirationScheduler
 *
 * PENDING 상태로 방치된 예약을 자동 만료시키는 스케줄러.
 *
 * 동작:
 *   - 1분마다 실행 (fixedDelay = 60_000ms)
 *   - createdAt 기준 10분(PENDING_TIMEOUT_MINUTES) 초과된 PENDING 예약 조회
 *   - 각 예약의 좌석 재고를 복구(SeatInventory.restore) 후 Booking.cancel() 처리
 *
 * 트랜잭션:
 *   - 좌석 복구와 예약 취소가 하나의 트랜잭션으로 묶여 정합성 보장
 *   - SeatInventory 조회 시 비관적 락(findForUpdate) 사용
 *
 * 관련: Booking, SeatInventory, BookingRepository, SeatInventoryRepository
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingExpirationScheduler {

    private final BookingRepository bookingRepository;
    private final SeatInventoryRepository seatInventoryRepository;

    private static final int PENDING_TIMEOUT_MINUTES = 10;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void expireStaleBookings() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(PENDING_TIMEOUT_MINUTES);
        List<Booking> expired = bookingRepository.findExpiredPending(threshold);

        if (expired.isEmpty()) return;

        log.info("[Scheduler] Expiring {} stale PENDING bookings", expired.size());
        for (Booking booking : expired) {
            seatInventoryRepository
                    .findForUpdate(booking.getFlightId(), booking.getSeatClass())
                    .ifPresent(inv -> inv.restore(booking.getPassengerCount()));
            booking.cancel();
        }
    }
}
