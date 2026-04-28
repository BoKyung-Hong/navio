package com.navio.domain.booking;

import com.navio.domain.seat.SeatClass;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Booking (엔티티)
 *
 * 항공권 예약 정보를 저장하는 JPA 엔티티. DB 테이블명: bookings
 *
 * 필드:
 *   - bookingNumber : 예약번호 (형식: NV{YYYYMMDD}{4자리 랜덤}, 예: NV202604270A1B)
 *   - userId        : 예약한 사용자 FK (User.id)
 *   - flightId      : 예약한 항공편 FK (Flight.id)
 *   - seatClass     : 좌석 등급 (ECONOMY | BUSINESS | FIRST)
 *   - passengerCount: 탑승객 수
 *   - totalPrice    : 총 결제 금액 = seatPrice × passengerCount
 *   - status        : 예약 상태 (PENDING → CONFIRMED | CANCELLED)
 *   - passengers    : 탑승객 목록 (OneToMany, cascade ALL)
 *
 * 상태 흐름:
 *   PENDING (예약 생성) → CONFIRMED (결제 완료) → CANCELLED (취소)
 *   PENDING는 10분 후 BookingExpirationScheduler에 의해 자동 CANCELLED 처리됨
 *
 * 관련: Passenger, BookingStatus, BookingService, BookingController
 */
@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_number", nullable = false, unique = true, length = 20)
    private String bookingNumber;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "flight_id", nullable = false)
    private Long flightId;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false, length = 20)
    private SeatClass seatClass;

    @Column(name = "passenger_count", nullable = false)
    private Integer passengerCount;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Passenger> passengers = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) this.status = BookingStatus.PENDING;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void confirm() { this.status = BookingStatus.CONFIRMED; }
    public void cancel()  { this.status = BookingStatus.CANCELLED; }
}
