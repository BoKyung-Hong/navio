package com.navio.domain.flight.dto;

import com.navio.domain.flight.Airport;
import com.navio.domain.flight.Flight;
import com.navio.domain.seat.SeatInventory;

import java.time.LocalDateTime;
import java.util.List;

/**
 * FlightResponse (DTO)
 *
 * 항공편 검색/상세 조회 API의 응답 DTO.
 *
 * 중첩 레코드:
 *   - Endpoint : 출발지/도착지 정보 (공항 코드, 한글명, 시간, 시간대)
 *   - SeatInfo : 좌석 등급별 잔여 좌석 수와 가격
 *   - Checkin  : 체크인 오픈/마감 기준 (출발 N분 전)
 *
 * from() 팩토리 메서드로 Flight, Airport, SeatInventory 엔티티를 조합해 생성한다.
 *
 * 관련: Flight, Airport, SeatInventory, FlightSearchService, FlightController
 */
public record FlightResponse(
        Long id,
        String flightNumber,
        String airline,
        Endpoint departure,
        Endpoint arrival,
        String aircraftType,
        List<SeatInfo> seats,
        Checkin checkin
) {
    public record Endpoint(String airport, String airportName, LocalDateTime time, String timezone) {}
    public record SeatInfo(String seatClass, int available, int price) {}
    public record Checkin(int openMinutesBefore, int closeMinutesBefore) {}

    public static FlightResponse from(Flight f, Airport dep, Airport arr, List<SeatInventory> seats) {
        return new FlightResponse(
                f.getId(),
                f.getFlightNumber(),
                f.getAirline(),
                new Endpoint(f.getDepartureAirport(),
                        dep != null ? dep.getNameKo() : null,
                        f.getDepartureTime(),
                        dep != null ? dep.getTimezone() : null),
                new Endpoint(f.getArrivalAirport(),
                        arr != null ? arr.getNameKo() : null,
                        f.getArrivalTime(),
                        arr != null ? arr.getTimezone() : null),
                f.getAircraftType(),
                seats.stream()
                        .map(s -> new SeatInfo(s.getSeatClass().name(), s.getAvailableSeats(), s.getPrice()))
                        .toList(),
                new Checkin(f.getCheckinOpenMinutes(), f.getCheckinCloseMinutes())
        );
    }
}
