package com.navio.domain.seat;

import com.navio.common.BusinessException;
import com.navio.common.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

/**
 * SeatInventory (엔티티)
 *
 * 항공편별·좌석 등급별 잔여 좌석 재고를 관리하는 JPA 엔티티. DB 테이블명: seat_inventory
 *
 * 복합 유니크 제약: (flight_id, seat_class) → 항공편당 등급별 하나의 재고 레코드만 허용
 *
 * 필드:
 *   - flightId       : 항공편 FK (Flight.id)
 *   - seatClass      : 좌석 등급 (ECONOMY | BUSINESS | FIRST)
 *   - totalSeats     : 전체 좌석 수 (예약 취소 시 복구 한도)
 *   - availableSeats : 현재 예약 가능 잔여 좌석 수
 *   - price          : 해당 등급의 1인당 가격 (원)
 *   - version        : 낙관적 락 버전 (@Version)
 *
 * 동시성:
 *   - BookingService.createBooking() 에서 findForUpdate(PESSIMISTIC_WRITE)로 조회 후 decrease() 호출
 *   - 잔여 좌석 부족 시 SOLD_OUT 예외 발생
 *
 * 관련: SeatClass, SeatInventoryRepository, BookingService, BookingExpirationScheduler
 */
@Entity
@Table(name = "seat_inventory",
        uniqueConstraints = @UniqueConstraint(columnNames = {"flight_id", "seat_class"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class SeatInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_id", nullable = false)
    private Long flightId;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false, length = 20)
    private SeatClass seatClass;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(nullable = false)
    private Integer price;

    /**
     * 초과 예약 비율 (0.05 = 5%). 노쇼 승객을 고려해 총 좌석의 5%까지 초과 판매 허용.
     * 판매 가능 상한 = (int)(totalSeats * (1 + overbookingRate))
     */
    @Column(name = "overbooking_rate", nullable = false)
    @Builder.Default
    private Double overbookingRate = 0.05;

    @Version
    private Long version;

    /** 좌석 차감. Overbooking 상한 초과 시 SOLD_OUT 예외. 호출 전 findForUpdate()로 비관적 락 획득 필수. */
    public void decrease(int count) {
        int capacity = (int)(this.totalSeats * (1 + this.overbookingRate));
        if (this.availableSeats < count || (this.totalSeats - this.availableSeats + count) > capacity) {
            throw new BusinessException(ErrorCode.SOLD_OUT);
        }
        this.availableSeats -= count;
    }

    /** 좌석 복구 (예약 취소·만료 시). totalSeats를 초과하지 않도록 보정. */
    public void restore(int count) {
        this.availableSeats = Math.min(this.totalSeats, this.availableSeats + count);
    }

    public void updatePrice(int price) {
        this.price = price;
    }
}
