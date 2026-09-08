package com.teamreports.weeklyreport.ai;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/** A tool definition in the shape the Anthropic Messages API expects. */
public record AnthropicTool(
        String name,
        String description,
        @JsonProperty("input_schema") Map<String, Object> inputSchema
) {
}
