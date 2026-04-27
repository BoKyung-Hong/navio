package com.navio.domain.flight;

import com.navio.common.BusinessException;
import com.navio.common.ErrorCode;
import com.navio.domain.flight.dto.FlightResponse;
import com.navio.domain.seat.SeatInventory;
import com.navio.domain.seat.SeatInventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * FlightSearchService
 *
 * 항공편 검색 및 상세 조회 비즈니스 로직.
 *
 * search():
 *   - 출발지·도착지·날짜로 항공편 목록 조회 (FlightRepository.searchByRouteAndDate)
 *   - 조회된 항공편의 SeatInventory를 배치 조회(flightIdIn)하여 N+1 방지
 *   - FlightResponse로 변환 (Airport 정보 포함)
 *
 * getDetail():
 *   - 단일 항공편 상세 조회 + SeatInventory 목록 + Airport 정보
 *
 * 관련: Flight, Airport, SeatInventory, FlightRepository, AirportRepository, SeatInventoryRepository
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightSearchService {

    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;
    private final SeatInventoryRepository seatInventoryRepository;

    public List<FlightResponse> search(String departure, String arrival, LocalDate date,
                                       String seatClass, int passengers) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        List<Flight> flights = flightRepository.searchByRouteAndDate(departure, arrival, start, end);

        Map<Long, List<SeatInventory>> seatMap = seatInventoryRepository
                .findByFlightIdIn(flights.stream().map(Flight::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(SeatInventory::getFlightId));

        return flights.stream()
                .map(f -> FlightResponse.from(f,
                        airportRepository.findById(f.getDepartureAirport()).orElse(null),
                        airportRepository.findById(f.getArrivalAirport()).orElse(null),
                        seatMap.getOrDefault(f.getId(), List.of())))
                .toList();
    }

    public FlightResponse getDetail(Long flightId) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FLIGHT_NOT_FOUND));
        List<SeatInventory> seats = seatInventoryRepository.findByFlightId(flightId);
        return FlightResponse.from(flight,
                airportRepository.findById(flight.getDepartureAirport()).orElse(null),
                airportRepository.findById(flight.getArrivalAirport()).orElse(null),
                seats);
    }
}
