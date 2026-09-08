package com.teamreports.weeklyreport.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Each entry in {@code content} is a block map with at least a "type" field
 * ("text" | "tool_use"), kept as a raw Map rather than a strict type because the
 * shape genuinely varies by block type and this is the only place that needs to
 * inspect it.
 */
public record AnthropicResponse(
        String id,
        String role,
        List<Map<String, Object>> content,
        @JsonProperty("stop_reason") String stopReason
) {
}
