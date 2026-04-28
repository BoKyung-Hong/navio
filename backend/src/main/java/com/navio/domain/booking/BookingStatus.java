package com.navio.domain.booking;

public enum BookingStatus {
    PENDING,          // 예약 생성, 결제 대기 (10분 내 결제 필수)
    CONFIRMED,        // 결제 완료
    TICKETED,         // 발권 완료 (결제 후 자동 전환)
    CANCEL_REQUESTED, // 취소 요청 (결제 완료 후 취소 → 환불 대기)
    CANCELLED,        // 취소 완료 (PENDING 취소 또는 환불 처리 후)
    REFUND_PENDING,   // 환불 진행중
    REFUNDED          // 환불 완료
}
