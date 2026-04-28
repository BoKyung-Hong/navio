package com.navio.domain.booking;

import com.navio.common.ApiResponse;
import com.navio.config.UserPrincipal;
import com.navio.domain.booking.dto.BookingResponse;
import com.navio.domain.booking.dto.CreateBookingRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * BookingController
 *
 * 예약 생성·조회·취소 REST API. JWT 인증 필수.
 *
 * POST /api/bookings                       → 예약 생성 (좌석 차감, PENDING 상태)
 * GET  /api/bookings                       → 내 예약 목록 (최신순)
 * GET  /api/bookings/{bookingNumber}       → 예약 상세 조회
 * POST /api/bookings/{bookingNumber}/cancel → 예약 취소 + 좌석 복구
 *
 * userId는 @AuthenticationPrincipal로 JWT에서 주입 (X-User-Id 헤더 방식 제거됨).
 *
 * 관련: BookingService, BookingResponse, CreateBookingRequest, UserPrincipal
 */
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> create(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateBookingRequest req
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(bookingService.createBooking(principal.getUserId(), req)));
    }

    @GetMapping
    public ApiResponse<List<BookingResponse>> myBookings(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(bookingService.myBookings(principal.getUserId()));
    }

    @GetMapping("/{bookingNumber}")
    public ApiResponse<BookingResponse> detail(@PathVariable String bookingNumber) {
        return ApiResponse.ok(bookingService.get(bookingNumber));
    }

    @PostMapping("/{bookingNumber}/cancel")
    public ApiResponse<BookingResponse> cancel(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String bookingNumber
    ) {
        return ApiResponse.ok(bookingService.cancel(principal.getUserId(), bookingNumber));
    }
}
