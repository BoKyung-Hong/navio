package com.navio.domain.flight;

import com.navio.common.ApiResponse;
import com.navio.common.BusinessException;
import com.navio.common.ErrorCode;
import com.navio.domain.booking.BookingRepository;
import com.navio.domain.booking.BookingStatus;
import com.navio.domain.flight.dto.FlightCreateRequest;
import com.navio.domain.flight.dto.FlightResponse;
import com.navio.domain.seat.SeatClass;
import com.navio.domain.seat.SeatInventory;
import com.navio.domain.seat.SeatInventoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/flights")
@RequiredArgsConstructor
public class AdminFlightController {

    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;
    private final SeatInventoryRepository seatInventoryRepository;
    private final BookingRepository bookingRepository;
    private final FlightSearchService flightSearchService;

    @GetMapping
    public ApiResponse<List<FlightResponse>> list() {
        List<Flight> flights = flightRepository.findAll();
        List<Long> ids = flights.stream().map(Flight::getId).toList();
        var seatMap = seatInventoryRepository.findByFlightIdIn(ids).stream()
                .collect(java.util.stream.Collectors.groupingBy(SeatInventory::getFlightId));
        List<FlightResponse> result = flights.stream()
                .map(f -> FlightResponse.from(f,
                        airportRepository.findById(f.getDepartureAirport()).orElse(null),
                        airportRepository.findById(f.getArrivalAirport()).orElse(null),
                        seatMap.getOrDefault(f.getId(), List.of())))
                .toList();
        return ApiResponse.ok(result);
    }

    @PostMapping
    @Transactional
    public ApiResponse<FlightResponse> create(@Valid @RequestBody FlightCreateRequest req) {
        Flight flight = Flight.builder()
                .flightNumber(req.flightNumber())
                .airline(req.airline())
                .departureAirport(req.departureAirport())
                .arrivalAirport(req.arrivalAirport())
                .departureTime(req.departureTime())
                .arrivalTime(req.arrivalTime())
                .aircraftType(req.aircraftType())
                .checkinOpenMinutes(req.checkinOpenMinutes() > 0 ? req.checkinOpenMinutes() : 1440)
                .checkinCloseMinutes(req.checkinCloseMinutes() > 0 ? req.checkinCloseMinutes() : 60)
                .baggageCarryOnKg(req.baggageCarryOnKg() > 0 ? req.baggageCarryOnKg() : 10)
                .baggageCarryOnSize(req.baggageCarryOnSize() != null ? req.baggageCarryOnSize() : "55x40x20")
                .baggageCheckedKg(req.baggageCheckedKg() > 0 ? req.baggageCheckedKg() : 23)
                .build();
        Flight saved = flightRepository.save(flight);

        seatInventoryRepository.save(SeatInventory.builder()
                .flightId(saved.getId()).seatClass(SeatClass.ECONOMY)
                .totalSeats(req.economySeats()).availableSeats(req.economySeats())
                .price(req.economyPrice()).overbookingRate(0.05).build());
        seatInventoryRepository.save(SeatInventory.builder()
                .flightId(saved.getId()).seatClass(SeatClass.BUSINESS)
                .totalSeats(req.businessSeats()).availableSeats(req.businessSeats())
                .price(req.businessPrice()).overbookingRate(0.05).build());

        return ApiResponse.ok(flightSearchService.getDetail(saved.getId()));
    }

    @PutMapping("/{flightId}")
    @Transactional
    public ApiResponse<FlightResponse> update(@PathVariable Long flightId,
                                              @Valid @RequestBody FlightCreateRequest req) {
        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new BusinessException(ErrorCode.FLIGHT_NOT_FOUND));
        flight.update(req.departureTime(), req.arrivalTime(), req.aircraftType());

        seatInventoryRepository.findByFlightId(flightId).forEach(inv -> {
            if (inv.getSeatClass() == SeatClass.ECONOMY) inv.updatePrice(req.economyPrice());
            else if (inv.getSeatClass() == SeatClass.BUSINESS) inv.updatePrice(req.businessPrice());
        });

        return ApiResponse.ok(flightSearchService.getDetail(flightId));
    }

    @DeleteMapping("/{flightId}")
    @Transactional
    public ApiResponse<Void> delete(@PathVariable Long flightId) {
        if (!flightRepository.existsById(flightId))
            throw new BusinessException(ErrorCode.FLIGHT_NOT_FOUND);

        List<BookingStatus> activeStatuses = List.of(
                BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.TICKETED);
        if (bookingRepository.existsByFlightIdAndStatusIn(flightId, activeStatuses))
            throw new BusinessException(ErrorCode.FORBIDDEN);

        seatInventoryRepository.deleteByFlightId(flightId);
        flightRepository.deleteById(flightId);
        return ApiResponse.ok(null);
    }
}
