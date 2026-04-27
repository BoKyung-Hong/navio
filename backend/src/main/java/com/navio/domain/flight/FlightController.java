package com.navio.domain.flight;

import com.navio.common.ApiResponse;
import com.navio.domain.flight.dto.FlightResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * FlightController
 *
 * 항공편 검색 및 상세 조회 REST API. 인증 불필요(공개 엔드포인트).
 *
 * GET /api/flights/search
 *   - departure (필수): 출발 공항 코드 (예: ICN)
 *   - arrival   (필수): 도착 공항 코드 (예: NRT)
 *   - date      (필수): 출발 날짜 (ISO: 2026-05-01)
 *   - seatClass (선택): ECONOMY | BUSINESS | FIRST
 *   - passengers (선택, 기본 1): 탑승객 수
 *
 * GET /api/flights/{flightId}
 *   - 항공편 ID로 상세 정보 조회
 *
 * 관련: FlightSearchService, FlightResponse, Flight, SeatInventory
 */
@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightSearchService flightSearchService;

    @GetMapping("/search")
    public ApiResponse<List<FlightResponse>> search(
            @RequestParam String departure,
            @RequestParam String arrival,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String seatClass,
            @RequestParam(defaultValue = "1") int passengers
    ) {
        return ApiResponse.ok(flightSearchService.search(departure, arrival, date, seatClass, passengers));
    }

    @GetMapping("/{flightId}")
    public ApiResponse<FlightResponse> detail(@PathVariable Long flightId) {
        return ApiResponse.ok(flightSearchService.getDetail(flightId));
    }
}
