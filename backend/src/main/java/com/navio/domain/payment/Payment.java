package com.navio.domain.payment;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Payment (엔티티)
 *
 * 결제 정보를 저장하는 JPA 엔티티. DB 테이블명: payments
 *
 * TossPayments의 결제 승인 응답 정보를 저장하며, Booking과 1:1 관계이다.
 *
 * 필드:
 *   - bookingId  : 연결된 예약 FK (Booking.id), 유니크
 *   - orderId    : TossPayments orderId = bookingNumber
 *   - paymentKey : TossPayments 결제 키 (승인 후 저장)
 *   - method     : 결제 수단 (CARD | TRANSFER | VIRTUAL_ACCOUNT)
 *   - amount     : 결제 금액 (원)
 *   - status     : 결제 상태 (READY → DONE | FAILED)
 *   - paidAt     : 실제 결제 완료 시각
 *
 * 상태 흐름: READY → markDone() → DONE
 *            READY → markFailed() → FAILED
 *
 * 관련: PaymentStatus, PaymentService, PaymentController, TossPaymentsClient
 */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false, unique = true)
    private Long bookingId;

    @Column(name = "order_id", nullable = false, unique = true, length = 64)
    private String orderId;

    @Column(name = "payment_key", length = 200)
    private String paymentKey;

    @Column(length = 20)
    private String method;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = PaymentStatus.READY;
    }

    public void markDone(String paymentKey, String method) {
        this.paymentKey = paymentKey;
        this.method = method;
        this.status = PaymentStatus.DONE;
        this.paidAt = LocalDateTime.now();
    }

    public void markFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }
}
