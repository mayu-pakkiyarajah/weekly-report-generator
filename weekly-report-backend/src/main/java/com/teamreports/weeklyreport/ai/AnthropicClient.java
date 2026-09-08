package com.teamreports.weeklyreport.ai;

import com.teamreports.weeklyreport.config.AiProperties;
import com.teamreports.weeklyreport.exception.AiServiceException;
import com.teamreports.weeklyreport.exception.AiUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Thin wrapper around the Anthropic Messages API (POST /v1/messages).
 * Uses Spring's synchronous {@link RestClient} - no extra dependency needed since
 * it ships with spring-web, already a transitive dependency of spring-boot-starter-web.
 */
@Component
@Slf4j
public class AnthropicClient {

    private final AiProperties aiProperties;
    private final RestClient restClient;

    public AnthropicClient(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
        this.restClient = RestClient.builder()
                .baseUrl(aiProperties.apiBaseUrl())
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("content-type", "application/json")
                .build();
    }

    public AnthropicResponse createMessage(AnthropicRequest request) {
        if (!aiProperties.isEnabledAndConfigured()) {
            throw new AiUnavailableException(
                    "The AI assistant is not configured. Set AI_ASSISTANT_ENABLED=true and ANTHROPIC_API_KEY to enable it.");
        }

        try {
            return restClient.post()
                    .uri("/v1/messages")
                    .header("x-api-key", aiProperties.apiKey())
                    .body(request)
                    .retrieve()
                    .body(AnthropicResponse.class);
        } catch (RestClientResponseException ex) {
            log.error("Anthropic API call failed: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new AiServiceException("The AI assistant could not process this request right now.", ex);
        }
    }
}
