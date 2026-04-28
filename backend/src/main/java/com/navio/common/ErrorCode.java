package com.navio.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * ErrorCode
 *
 * 서비스 전역 에러 코드 열거형.
 * GlobalExceptionHandler에서 BusinessException을 잡아 이 코드를 기반으로 HTTP 응답을 반환한다.
 *
 * 각 코드는 (HTTP 상태 코드, 사용자 메시지)를 보유한다.
 * 프론트엔드는 error.code 필드로 에러 종류를 구분한다.
 */
@Getter
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "잘못된 입력값입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // 인증 (Auth)
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    // 항공편 (Flight)
    FLIGHT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 항공편을 찾을 수 없습니다."),
    AIRPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 공항을 찾을 수 없습니다."),

    // 좌석 / 예약 (Seat / Booking)
    SOLD_OUT(HttpStatus.CONFLICT, "좌석이 부족합니다."),
    BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."),
    BOOKING_EXPIRED(HttpStatus.GONE, "결제 마감 시간이 지났습니다."),
    ALREADY_CANCELLED(HttpStatus.CONFLICT, "이미 취소된 예약입니다."),

    // 결제 (Payment)
    AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "결제 금액이 일치하지 않습니다."),
    PAYMENT_FAILED(HttpStatus.BAD_REQUEST, "결제에 실패했습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 내역을 찾을 수 없습니다."),

    // 사용자
    WRONG_PASSWORD(HttpStatus.BAD_REQUEST, "현재 비밀번호가 올바르지 않습니다."),

    // AI 채팅
    AI_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "AI 서비스를 현재 사용할 수 없습니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
