package com.navio.domain.flight;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FlightRepository
 *
 * Flight 엔티티의 JPA 레포지토리.
 *
 * searchByRouteAndDate:
 *   출발지·도착지 코드와 날짜 범위(00:00 ~ 23:59)로 항공편 목록 조회.
 *   flights 테이블의 idx_flight_search 인덱스를 활용해 성능을 최적화한다.
 */
public interface FlightRepository extends JpaRepository<Flight, Long> {

    @Query("""
        SELECT f FROM Flight f
        WHERE f.departureAirport = :departure
          AND f.arrivalAirport = :arrival
          AND f.departureTime BETWEEN :dayStart AND :dayEnd
        ORDER BY f.departureTime ASC
    """)
    List<Flight> searchByRouteAndDate(
            @Param("departure") String departure,
            @Param("arrival") String arrival,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd
    );

    boolean existsByDepartureTimeAfter(LocalDateTime time);
}
