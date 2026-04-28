package com.navio.domain.chat;

import com.navio.common.ApiResponse;
import com.navio.config.UserPrincipal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AiChatController
 *
 * AI 여행 도우미 REST API. JWT 인증 필수.
 *
 * POST /api/chat/booking/{bookingNumber}
 *   - 예약 상세 기반 Claude AI 답변 반환
 *   - 요청: { "message": "질문 내용" }
 *   - 응답: { "reply": "AI 답변" }
 *   - 본인 예약이 아닌 경우 403 반환
 *
 * 관련: AiChatService, AnthropicClient
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    public record ChatRequest(@NotBlank String message) {}

    @PostMapping("/booking/{bookingNumber}")
    public ApiResponse<Map<String, String>> chat(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable String bookingNumber,
            @Valid @RequestBody ChatRequest req) {
        String reply = aiChatService.chat(principal.getUserId(), bookingNumber, req.message());
        return ApiResponse.ok(Map.of("reply", reply));
    }
}
