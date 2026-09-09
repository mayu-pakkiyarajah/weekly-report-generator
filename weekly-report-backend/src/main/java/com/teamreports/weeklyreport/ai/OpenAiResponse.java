package com.teamreports.weeklyreport.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record OpenAiResponse(List<Choice> choices) {

    public record Choice(Message message, @JsonProperty("finish_reason") String finishReason) {
    }

    /**
     * {@code toolCalls} entries are raw maps - each has "id", "type", and a nested
     * "function" map with "name" and "arguments" (a JSON-encoded *string*, not an
     * object - OpenAI's one real divergence from Anthropic's tool_use shape that the
     * calling code needs to handle explicitly).
     */
    public record Message(String role, String content, @JsonProperty("tool_calls") List<Map<String, Object>> toolCalls) {
    }
}
