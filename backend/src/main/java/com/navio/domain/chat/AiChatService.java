package com.navio.domain.chat;

import com.navio.common.BusinessException;
import com.navio.common.ErrorCode;
import com.navio.domain.booking.Booking;
import com.navio.domain.booking.BookingRepository;
import com.navio.domain.flight.Flight;
import com.navio.domain.flight.FlightRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

/**
 * AiChatService
 *
 * AI 여행 도우미 비즈니스 로직.
 *
 * chat():
 *   1. bookingNumber로 예약 조회 (본인 예약 확인)
 *   2. 연결된 항공편 조회
 *   3. 예약·항공편 정보를 System Prompt에 포함
 *   4. AnthropicClient.chat() 호출 → 응답 반환
 *
 * System Prompt에 예약 컨텍스트를 주입하므로 RAG 파이프라인 없이도
 * 예약별 맞춤 답변을 제공한다.
 *
 * 관련: AnthropicClient, Booking, Flight, AiChatController
 */
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final AnthropicClient anthropicClient;

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Transactional(readOnly = true)
    public String chat(Long userId, String bookingNumber, String userMessage) {
        Booking booking = bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));

        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Flight flight = flightRepository.findById(booking.getFlightId())
                .orElseThrow(() -> new BusinessException(ErrorCode.FLIGHT_NOT_FOUND));

        String systemPrompt = buildSystemPrompt(booking, flight);
        return anthropicClient.chat(systemPrompt, userMessage);
    }

    private String buildSystemPrompt(Booking booking, Flight flight) {
        return """
                당신은 Navio 항공 예약 서비스의 AI 여행 도우미입니다.
                친절하고 간결하게 한국어로 답변해주세요.

                현재 사용자의 예약 정보는 다음과 같습니다:
                - 예약번호: %s
                - 예약 상태: %s
                - 항공편: %s %s (%s → %s)
                - 출발: %s
                - 도착: %s
                - 좌석 등급: %s
                - 탑승객 수: %d명
                - 결제 금액: %,d원
                - 체크인 오픈: 출발 %d시간 전 (= %s)
                - 체크인 마감: 출발 %d분 전 (= %s)
                - 기내 수하물: %dkg 이하, %s cm 이내
                - 위탁 수하물: %dkg 이하

                위 정보를 바탕으로 사용자 질문에 답변해주세요.
                예약 정보에 없는 내용은 일반적인 항공 상식으로 답변하되, 추측임을 명시하세요.
                """.formatted(
                booking.getBookingNumber(),
                booking.getStatus().name(),
                flight.getAirline(), flight.getFlightNumber(),
                flight.getDepartureAirport(), flight.getArrivalAirport(),
                flight.getDepartureTime().format(DT_FMT),
                flight.getArrivalTime().format(DT_FMT),
                booking.getSeatClass().name(),
                booking.getPassengerCount(),
                booking.getTotalPrice(),
                flight.getCheckinOpenMinutes() / 60,
                flight.getDepartureTime().minusMinutes(flight.getCheckinOpenMinutes()).format(DT_FMT),
                flight.getCheckinCloseMinutes(),
                flight.getDepartureTime().minusMinutes(flight.getCheckinCloseMinutes()).format(DT_FMT),
                flight.getBaggageCarryOnKg(), flight.getBaggageCarryOnSize(),
                flight.getBaggageCheckedKg()
        );
    }
}
