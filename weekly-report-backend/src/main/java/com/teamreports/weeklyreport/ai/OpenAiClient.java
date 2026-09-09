package com.teamreports.weeklyreport.ai;

import com.teamreports.weeklyreport.config.AiProperties;
import com.teamreports.weeklyreport.exception.AiServiceException;
import com.teamreports.weeklyreport.exception.AiUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

/**
 * Thin wrapper around OpenAI's Chat Completions API (POST /v1/chat/completions).
 * Uses Spring's synchronous {@link RestClient} - no extra dependency needed since
 * it ships with spring-web, already a transitive dependency of spring-boot-starter-web.
 */
@Component
@Slf4j
public class OpenAiClient {

    private final AiProperties aiProperties;
    private final RestClient restClient;

    public OpenAiClient(AiProperties aiProperties) {
        this.aiProperties = aiProperties;
        this.restClient = RestClient.builder()
                .baseUrl(aiProperties.apiBaseUrl())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public OpenAiResponse createChatCompletion(OpenAiRequest request) {
        if (!aiProperties.isEnabledAndConfigured()) {
            throw new AiUnavailableException(
                    "The AI assistant is not configured. Set AI_ASSISTANT_ENABLED=true and OPENAI_API_KEY to enable it.");
        }

        try {
            return restClient.post()
                    .uri("/v1/chat/completions")
                    .header("Authorization", "Bearer " + aiProperties.apiKey())
                    .body(request)
                    .retrieve()
                    .body(OpenAiResponse.class);
        } catch (RestClientResponseException ex) {
            log.error("OpenAI API call failed: {} - {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new AiServiceException("The AI assistant could not process this request right now.", ex);
        }
    }
}
