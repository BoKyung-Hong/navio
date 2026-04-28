package com.navio.domain.flight;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Flight (엔티티)
 *
 * 항공편 정보를 저장하는 JPA 엔티티. DB 테이블명: flights
 *
 * 필드:
 *   - flightNumber      : 항공편 번호 (예: OZ102)
 *   - airline           : 항공사 이름 (예: 아시아나항공)
 *   - departureAirport  : 출발 공항 코드 (3자리, 예: ICN)
 *   - arrivalAirport    : 도착 공항 코드 (3자리, 예: NRT)
 *   - departureTime     : 출발 일시 (LocalDateTime, 공항 현지시간 기준)
 *   - arrivalTime       : 도착 일시 (LocalDateTime, 공항 현지시간 기준)
 *   - aircraftType      : 기종 (예: A380, B777)
 *   - checkinOpenMinutes  : 체크인 오픈 시간 (출발 N분 전, 기본 1440 = 24시간)
 *   - checkinCloseMinutes : 체크인 마감 시간 (출발 N분 전, 기본 60)
 *
 * 인덱스: departure_airport + arrival_airport + departure_time (검색 최적화)
 *
 * 관련: Airport, SeatInventory, FlightSearchService, FlightRepository
 */
@Entity
@Table(name = "flights",
       indexes = {
           @Index(name = "idx_flight_search", columnList = "departure_airport,arrival_airport,departure_time")
       })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_number", nullable = false, length = 10)
    private String flightNumber;

    @Column(nullable = false, length = 50)
    private String airline;

    @Column(name = "departure_airport", nullable = false, length = 3)
    private String departureAirport;

    @Column(name = "arrival_airport", nullable = false, length = 3)
    private String arrivalAirport;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "aircraft_type", length = 20)
    private String aircraftType;

    @Column(name = "checkin_open_minutes", nullable = false)
    private Integer checkinOpenMinutes;

    @Column(name = "checkin_close_minutes", nullable = false)
    private Integer checkinCloseMinutes;

    /** 기내 수하물 허용 무게 (kg). 기본 10kg. */
    @Column(name = "baggage_carry_on_kg", nullable = false)
    private Integer baggageCarryOnKg;

    /** 기내 수하물 허용 사이즈 (가로x세로x높이 cm). 기본 55x40x20. */
    @Column(name = "baggage_carry_on_size", nullable = false, length = 20)
    private String baggageCarryOnSize;

    /** 위탁 수하물 허용 무게 (kg). 기본 23kg. */
    @Column(name = "baggage_checked_kg", nullable = false)
    private Integer baggageCheckedKg;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.checkinOpenMinutes == null) this.checkinOpenMinutes = 1440;
        if (this.checkinCloseMinutes == null) this.checkinCloseMinutes = 60;
        if (this.baggageCarryOnKg == null) this.baggageCarryOnKg = 10;
        if (this.baggageCarryOnSize == null) this.baggageCarryOnSize = "55x40x20";
        if (this.baggageCheckedKg == null) this.baggageCheckedKg = 23;
    }

    public void update(LocalDateTime departureTime, LocalDateTime arrivalTime, String aircraftType) {
        if (departureTime != null) this.departureTime = departureTime;
        if (arrivalTime != null) this.arrivalTime = arrivalTime;
        if (aircraftType != null && !aircraftType.isBlank()) this.aircraftType = aircraftType;
    }
}
