package com.navio.domain.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navio.common.BusinessException;
import com.navio.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

/**
 * TossPaymentsClient
 *
 * TossPayments REST API 호출 래퍼 컴포넌트.
 *
 * 역할:
 *   - 결제 승인 API(POST /payments/confirm) 호출
 *   - Secret Key를 Base64로 인코딩하여 Basic Auth 헤더에 포함
 *   - 4xx 오류(결제 실패, 금액 불일치 등) → PAYMENT_FAILED 예외
 *   - 5xx/네트워크 오류 → PAYMENT_FAILED 예외
 *
 * 보안:
 *   - secretKey는 서버 환경변수(TOSS_SECRET_KEY)에서만 주입, 클라이언트에 절대 노출 금지
 *   - application-local.yml의 test_sk_* 키는 테스트 환경 전용
 *
 * 관련: PaymentService, application.yml(navio.toss.*)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TossPaymentsClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${navio.toss.secret-key}")
    private String secretKey;

    @Value("${navio.toss.api-base-url}")
    private String baseUrl;

    public JsonNode cancel(String paymentKey, String cancelReason) {
        String url = baseUrl + "/payments/" + paymentKey + "/cancel";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(Base64.getEncoder().encodeToString((secretKey + ":").getBytes()));

        Map<String, Object> body = Map.of("cancelReason", cancelReason);
        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (HttpClientErrorException e) {
            log.warn("TossPayments cancel failed: {}", e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("TossPayments cancel error", e);
            throw new BusinessException(ErrorCode.PAYMENT_FAILED);
        }
    }

    /**
     * TossPayments 결제 승인 요청.
     * @return 승인 응답 JSON (status, method, totalAmount, approvedAt 등)
     */
    public JsonNode confirm(String paymentKey, String orderId, int amount) {
        String url = baseUrl + "/payments/confirm";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(Base64.getEncoder().encodeToString((secretKey + ":").getBytes()));

        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );

        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            return objectMapper.readTree(response.getBody());
        } catch (HttpClientErrorException e) {
            log.warn("TossPayments confirm failed: {}", e.getResponseBodyAsString());
            throw new BusinessException(ErrorCode.PAYMENT_FAILED, e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("TossPayments confirm error", e);
            throw new BusinessException(ErrorCode.PAYMENT_FAILED);
        }
    }
}
