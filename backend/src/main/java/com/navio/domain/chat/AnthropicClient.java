package com.navio.domain.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.navio.common.BusinessException;
import com.navio.common.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * AnthropicClient
 *
 * Claude API (claude-haiku-4-5) 호출 래퍼 컴포넌트.
 *
 * chat():
 *   - POST https://api.anthropic.com/v1/messages
 *   - x-api-key 헤더에 secretKey 포함
 *   - systemPrompt로 예약 컨텍스트 주입, userMessage로 실제 질문 전달
 *   - 응답 content[0].text 반환
 *
 * 보안: apiKey는 환경변수(CLAUDE_API_KEY)에서만 주입. 절대 클라이언트에 노출 금지.
 *
 * 관련: AiChatService, application.yml(navio.claude.*)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnthropicClient {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${navio.claude.api-key:}")
    private String apiKey;

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String MODEL = "claude-haiku-4-5-20251001";

    public String chat(String systemPrompt, String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(ErrorCode.AI_UNAVAILABLE);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-api-key", apiKey);
        headers.set("anthropic-version", "2023-06-01");

        Map<String, Object> body = Map.of(
                "model", MODEL,
                "max_tokens", 1024,
                "system", systemPrompt,
                "messages", List.of(Map.of("role", "user", "content", userMessage))
        );

        try {
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(API_URL, request, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            return root.path("content").get(0).path("text").asText();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Claude API error", e);
            throw new BusinessException(ErrorCode.AI_UNAVAILABLE);
        }
    }
}
