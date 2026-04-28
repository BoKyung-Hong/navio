package com.navio.domain.payment;

public enum PaymentStatus {
    READY,         // 결제 준비
    IN_PROGRESS,   // 결제 진행 중
    DONE,          // 결제 완료
    CANCELED,      // 결제 취소
    FAILED         // 결제 실패
}
