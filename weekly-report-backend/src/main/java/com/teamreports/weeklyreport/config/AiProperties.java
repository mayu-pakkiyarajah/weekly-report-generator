package com.teamreports.weeklyreport.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the optional AI chat assistant (assignment section 8).
 * The API key is never hardcoded - it must be supplied via the ANTHROPIC_API_KEY
 * environment variable. When absent, {@link com.teamreports.weeklyreport.service.ai.AiChatService}
 * reports the feature as unavailable rather than failing requests unpredictably.
 */
@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(
        boolean enabled,
        String apiKey,
        String model,
        String apiBaseUrl,
        int maxToolIterations,
        int maxOutputTokens
) {
    public boolean isEnabledAndConfigured() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }
}
