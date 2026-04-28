package com.navio.domain.booking;

import com.navio.common.BusinessException;
import com.navio.common.ErrorCode;
import com.navio.domain.alarm.AlarmService;
import com.navio.domain.booking.dto.BookingResponse;
import com.navio.domain.booking.dto.CreateBookingRequest;
import com.navio.domain.flight.Flight;
import com.navio.domain.flight.FlightRepository;
import com.navio.domain.payment.PaymentRepository;
import com.navio.domain.payment.TossPaymentsClient;
import com.navio.domain.seat.SeatClass;
import com.navio.domain.seat.SeatInventory;
import com.navio.domain.seat.SeatInventoryRepository;
import com.navio.domain.user.User;
import com.navio.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * BookingService
 *
 * 예약 생성·조회·취소 비즈니스 로직.
 *
 * createBooking():
 *   - SeatInventory 비관적 락 획득 → 좌석 차감 → Booking + Passenger 생성 → 원자적 저장
 *   - 좌석 부족 시 SOLD_OUT 예외 (409 Conflict)
 *   - 예약번호 형식: NV{YYYYMMDD}{A-Z0-9 4자리 랜덤}
 *   - 예약 생성 후 AlarmService.register()로 6종 알람 등록
 *
 * cancel():
 *   - 본인 예약인지 확인 (userId 불일치 → FORBIDDEN)
 *   - CANCELLED/CANCEL_REQUESTED → ALREADY_CANCELLED
 *   - PENDING 취소: 좌석 복구 → CANCELLED (환불 없음)
 *   - CONFIRMED/TICKETED 취소: 좌석 복구 → CANCEL_REQUESTED (환불 대기)
 *
 * 관련: Booking, Passenger, SeatInventory, AlarmService, BookingRepository
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    private final FlightRepository flightRepository;
    private final UserRepository userRepository;
    private final AlarmService alarmService;
    private final PaymentRepository paymentRepository;
    private final TossPaymentsClient tossPaymentsClient;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Transactional
    public BookingResponse createBooking(Long userId, CreateBookingRequest req) {
        SeatClass seatClass = SeatClass.valueOf(req.seatClass());

        SeatInventory inventory = seatInventoryRepository
                .findForUpdate(req.flightId(), seatClass)
                .orElseThrow(() -> new BusinessException(ErrorCode.FLIGHT_NOT_FOUND));

        int passengerCount = req.passengers().size();
        inventory.decrease(passengerCount);

        Booking booking = Booking.builder()
                .bookingNumber(generateBookingNumber())
                .userId(userId)
                .flightId(req.flightId())
                .seatClass(seatClass)
                .passengerCount(passengerCount)
                .totalPrice(inventory.getPrice() * passengerCount)
                .status(BookingStatus.PENDING)
                .build();

        for (CreateBookingRequest.PassengerInput p : req.passengers()) {
            booking.getPassengers().add(Passenger.builder()
                    .booking(booking)
                    .nameKorean(p.nameKorean())
                    .nameEnglish(p.nameEnglish())
                    .birthDate(p.birthDate())
                    .gender(p.gender())
                    .passportNumber(p.passportNumber())
                    .nationality(p.nationality())
                    .build());
        }

        Booking saved = bookingRepository.save(booking);

        flightRepository.findById(req.flightId()).ifPresent(flight -> {
            userRepository.findById(userId).ifPresent(user ->
                    alarmService.register(saved, flight, user.getEmail()));
        });

        return BookingResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> myBookings(Long userId) {
        return bookingRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(BookingResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse get(String bookingNumber) {
        return BookingResponse.from(findBooking(bookingNumber));
    }

    @Transactional
    public BookingResponse cancel(Long userId, String bookingNumber) {
        Booking booking = findBooking(bookingNumber);

        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
        if (booking.getStatus() == BookingStatus.CANCELLED
                || booking.getStatus() == BookingStatus.CANCEL_REQUESTED
                || booking.getStatus() == BookingStatus.REFUND_PENDING
                || booking.getStatus() == BookingStatus.REFUNDED) {
            throw new BusinessException(ErrorCode.ALREADY_CANCELLED);
        }

        seatInventoryRepository
                .findForUpdate(booking.getFlightId(), booking.getSeatClass())
                .ifPresent(inv -> inv.restore(booking.getPassengerCount()));

        if (booking.getStatus() == BookingStatus.PENDING) {
            booking.cancel();
        } else {
            // CONFIRMED / TICKETED → TossPayments 취소 API 호출 후 즉시 환불
            paymentRepository.findByBookingId(booking.getId()).ifPresentOrElse(payment -> {
                try {
                    tossPaymentsClient.cancel(payment.getPaymentKey(), "고객 취소 요청");
                    payment.markCanceled();
                    booking.refund();
                } catch (Exception e) {
                    log.warn("Toss cancel failed for booking {}: {}", bookingNumber, e.getMessage());
                    booking.startRefund();  // REFUND_PENDING → 스케줄러가 재시도
                }
            }, booking::requestCancel);
        }

        return BookingResponse.from(booking);
    }

    private Booking findBooking(String bookingNumber) {
        return bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));
    }

    private String generateBookingNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        StringBuilder sb = new StringBuilder("NV").append(date);
        for (int i = 0; i < 4; i++) sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        return sb.toString();
    }
}
