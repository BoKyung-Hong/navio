package com.navio.domain.booking;

import com.navio.common.ApiResponse;
import com.navio.common.BusinessException;
import com.navio.common.ErrorCode;
import com.navio.domain.booking.dto.BookingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final BookingRepository bookingRepository;

    @GetMapping
    public ApiResponse<List<BookingResponse>> list(
            @RequestParam(required = false) String status) {
        List<Booking> bookings;
        if (status != null && !status.isBlank()) {
            bookings = bookingRepository.findByStatus(BookingStatus.valueOf(status.toUpperCase()));
        } else {
            bookings = bookingRepository.findAllByOrderByCreatedAtDesc();
        }
        return ApiResponse.ok(bookings.stream().map(BookingResponse::from).toList());
    }

    @PatchMapping("/{bookingNumber}/status")
    @Transactional
    public ApiResponse<BookingResponse> updateStatus(
            @PathVariable String bookingNumber,
            @RequestBody Map<String, String> body) {
        Booking booking = bookingRepository.findByBookingNumber(bookingNumber)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOKING_NOT_FOUND));
        String newStatus = body.get("status");
        if (newStatus == null) throw new BusinessException(ErrorCode.INVALID_INPUT);

        switch (newStatus.toUpperCase()) {
            case "CONFIRMED"       -> booking.confirm();
            case "TICKETED"        -> booking.ticket();
            case "CANCEL_REQUESTED"-> booking.requestCancel();
            case "REFUND_PENDING"  -> booking.startRefund();
            case "REFUNDED"        -> booking.refund();
            case "CANCELLED"       -> booking.cancel();
            default -> throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        return ApiResponse.ok(BookingResponse.from(booking));
    }
}
