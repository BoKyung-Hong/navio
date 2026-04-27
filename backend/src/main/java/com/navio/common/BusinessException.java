package com.navio.common;

import lombok.Getter;

/**
 * BusinessException
 *
 * 비즈니스 로직에서 발생하는 예외의 기반 클래스.
 * ErrorCode를 포함하며, GlobalExceptionHandler가 자동으로 캐치하여
 * ErrorCode에 정의된 HTTP 상태 코드와 메시지로 ApiResponse를 반환한다.
 *
 * 사용 예:
 *   throw new BusinessException(ErrorCode.SOLD_OUT);
 *   throw new BusinessException(ErrorCode.PAYMENT_FAILED, "Toss status=CANCELED");
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}
