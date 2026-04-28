package com.navio.domain.booking;

public enum BookingStatus {
    PENDING,      // 예약 생성, 결제 대기
    CONFIRMED,    // 결제 완료
    CANCELLED,    // 취소됨
    REFUNDED      // 환불 완료
}
