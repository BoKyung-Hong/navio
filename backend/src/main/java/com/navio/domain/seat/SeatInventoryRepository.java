package com.navio.domain.seat;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * SeatInventoryRepository
 *
 * SeatInventory 엔티티의 JPA 레포지토리.
 *
 * findForUpdate:
 *   비관적 쓰기 락(SELECT ... FOR UPDATE)으로 동시에 같은 좌석에 예약이 몰릴 때
 *   한 번에 하나의 트랜잭션만 재고를 수정할 수 있도록 보장한다.
 *   → BookingService.createBooking()에서 사용
 *
 * findByFlightIdIn:
 *   항공편 목록에 대한 좌석 재고 배치 조회 (N+1 방지)
 *   → FlightSearchService.search()에서 사용
 */
public interface SeatInventoryRepository extends JpaRepository<SeatInventory, Long> {

    List<SeatInventory> findByFlightId(Long flightId);

    List<SeatInventory> findByFlightIdIn(List<Long> flightIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s FROM SeatInventory s
        WHERE s.flightId = :flightId AND s.seatClass = :seatClass
    """)
    Optional<SeatInventory> findForUpdate(@Param("flightId") Long flightId,
                                          @Param("seatClass") SeatClass seatClass);

    void deleteByFlightId(Long flightId);
}
