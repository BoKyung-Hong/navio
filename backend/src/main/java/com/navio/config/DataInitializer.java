package com.navio.config;

import com.navio.domain.flight.Airport;
import com.navio.domain.flight.AirportRepository;
import com.navio.domain.flight.Flight;
import com.navio.domain.flight.FlightRepository;
import com.navio.domain.seat.SeatClass;
import com.navio.domain.seat.SeatInventory;
import com.navio.domain.seat.SeatInventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DataInitializer
 *
 * 로컬 개발 환경 전용 시드 데이터 주입기. @Profile("local") 활성 시에만 동작한다.
 * 앱 최초 실행 시 Airport, Flight, SeatInventory를 자동으로 생성한다.
 * 이미 데이터가 있으면 스킵한다.
 *
 * 시드 데이터:
 *   - 공항 6개: ICN, NRT, KIX, LAX, JFK, CDG
 *   - 항공편: 오늘로부터 7일간 ICN→NRT(2편/일), ICN→LAX(1편/일), ICN→CDG(격일)
 *   - 좌석 재고: 각 항공편마다 ECONOMY(180석), BUSINESS(24석)
 *
 * 관련 엔티티: Airport, Flight, SeatInventory
 */
@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AirportRepository airportRepository;
    private final FlightRepository flightRepository;
    private final SeatInventoryRepository seatInventoryRepository;

    @Override
    public void run(String... args) {
        if (airportRepository.count() == 0) {
            airportRepository.saveAll(List.of(
                    Airport.builder().code("ICN").nameKo("인천국제공항").nameEn("Incheon Intl").city("Seoul").country("KR").timezone("Asia/Seoul").build(),
                    Airport.builder().code("NRT").nameKo("나리타국제공항").nameEn("Narita Intl").city("Tokyo").country("JP").timezone("Asia/Tokyo").build(),
                    Airport.builder().code("KIX").nameKo("간사이국제공항").nameEn("Kansai Intl").city("Osaka").country("JP").timezone("Asia/Tokyo").build(),
                    Airport.builder().code("LAX").nameKo("로스앤젤레스 국제공항").nameEn("Los Angeles Intl").city("Los Angeles").country("US").timezone("America/Los_Angeles").build(),
                    Airport.builder().code("JFK").nameKo("존 F. 케네디 국제공항").nameEn("John F. Kennedy Intl").city("New York").country("US").timezone("America/New_York").build(),
                    Airport.builder().code("CDG").nameKo("샤를 드 골 공항").nameEn("Charles de Gaulle").city("Paris").country("FR").timezone("Europe/Paris").build()
            ));
        }

        if (flightRepository.existsByDepartureTimeAfter(LocalDateTime.now())) {
            log.info("[DataInitializer] Future flights already present. Skip.");
            return;
        }

        log.info("[DataInitializer] Inserting seed data...");

        LocalDate today = LocalDate.now();
        for (int i = 1; i <= 7; i++) {
            LocalDate d = today.plusDays(i);
            createFlight("OZ102", "아시아나항공", "ICN", "NRT", d.atTime(9, 0),  d.atTime(11, 30), "A380", 350_000, 1_200_000);
            createFlight("KE702", "대한항공",     "ICN", "NRT", d.atTime(14, 0), d.atTime(16, 30), "B777", 320_000, 1_100_000);
            createFlight("KE017", "대한항공",     "ICN", "LAX", d.atTime(19, 0), d.atTime(13, 0).plusHours(1), "B747", 1_500_000, 5_500_000);
            if (i % 2 == 1) {
                createFlight("AF267", "에어프랑스", "ICN", "CDG", d.atTime(12, 30), d.atTime(18, 0), "A350", 1_700_000, 6_200_000);
            }
        }

        log.info("[DataInitializer] Done. flights={}", flightRepository.count());
    }

    private void createFlight(String number, String airline, String from, String to,
                              LocalDateTime depart, LocalDateTime arrive, String aircraft,
                              int economyPrice, int businessPrice) {
        Flight flight = Flight.builder()
                .flightNumber(number).airline(airline)
                .departureAirport(from).arrivalAirport(to)
                .departureTime(depart).arrivalTime(arrive)
                .aircraftType(aircraft)
                .checkinOpenMinutes(1440).checkinCloseMinutes(60)
                .baggageCarryOnKg(10).baggageCarryOnSize("55x40x20").baggageCheckedKg(23)
                .build();
        Flight saved = flightRepository.save(flight);

        seatInventoryRepository.save(SeatInventory.builder()
                .flightId(saved.getId()).seatClass(SeatClass.ECONOMY)
                .totalSeats(180).availableSeats(180).price(economyPrice)
                .overbookingRate(0.05).build());
        seatInventoryRepository.save(SeatInventory.builder()
                .flightId(saved.getId()).seatClass(SeatClass.BUSINESS)
                .totalSeats(24).availableSeats(24).price(businessPrice)
                .overbookingRate(0.05).build());
    }
}
