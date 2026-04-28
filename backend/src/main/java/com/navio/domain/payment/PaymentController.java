package com.navio.domain.payment;

import com.navio.common.ApiResponse;
import com.navio.common.BusinessException;
import com.navio.common.ErrorCode;
import com.navio.domain.payment.dto.ConfirmPaymentRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PaymentController
 *
 * TossPayments 결제 승인 및 조회 REST API. JWT 인증 필수.
 *
 * POST /api/payments/confirm
 *   - 프론트엔드 TossPayments 위젯 결제 완료 후 successUrl 리다이렉트에서 호출
 *   - paymentKey, orderId(= bookingNumber), amount를 서버에서 최종 검증
 *   - 성공 시 Payment 저장 + Booking → CONFIRMED
 *
 * GET /api/payments/{orderId}
 *   - orderId(= bookingNumber)로 결제 상태 조회
 *
 * 관련: PaymentService, Payment, Booking, TossPaymentsClient
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;

    @PostMapping("/confirm")
    public ApiResponse<Map<String, Object>> confirm(@Valid @RequestBody ConfirmPaymentRequest req) {
        Payment payment = paymentService.confirm(req);
        return ApiResponse.ok(Map.of(
                "paymentId", payment.getId(),
                "status", payment.getStatus().name(),
                "method", payment.getMethod() != null ? payment.getMethod() : "",
                "paidAt", payment.getPaidAt()
        ));
    }

    @GetMapping("/{orderId}")
    public ApiResponse<Map<String, Object>> get(@PathVariable String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PAYMENT_NOT_FOUND));
        return ApiResponse.ok(Map.of(
                "orderId", payment.getOrderId(),
                "status", payment.getStatus().name(),
                "amount", payment.getAmount()
        ));
    }
}
