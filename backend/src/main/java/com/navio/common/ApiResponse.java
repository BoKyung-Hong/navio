package com.navio.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * ApiResponse<T>
 *
 * 모든 REST API의 공통 응답 래퍼.
 *
 * 성공 응답:
 *   { "success": true, "data": { ... } }
 *
 * 실패 응답:
 *   { "success": false, "error": { "code": "FLIGHT_NOT_FOUND", "message": "..." } }
 *
 * 사용 예:
 *   return ApiResponse.ok(data);       // 성공
 *   return ApiResponse.fail(ErrorCode.FLIGHT_NOT_FOUND);  // 실패
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ErrorBody error;

    private ApiResponse(boolean success, T data, ErrorBody error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(true, null, null);
    }

    public static ApiResponse<Void> fail(ErrorCode code) {
        return new ApiResponse<>(false, null, new ErrorBody(code.name(), code.getMessage()));
    }

    public static ApiResponse<Void> fail(ErrorCode code, String customMessage) {
        return new ApiResponse<>(false, null, new ErrorBody(code.name(), customMessage));
    }

    /**
     * 실패 응답의 에러 정보 객체.
     * code: 에러 식별자 (예: "SOLD_OUT"), message: 사용자 노출용 메시지
     */
    @Getter
    public static class ErrorBody {
        private final String code;
        private final String message;

        public ErrorBody(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
