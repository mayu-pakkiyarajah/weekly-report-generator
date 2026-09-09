package com.teamreports.weeklyreport.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiRequest(
        String model,
        List<OpenAiMessage> messages,
        List<OpenAiTool> tools,
        @JsonProperty("tool_choice") String toolChoice,
        @JsonProperty("max_tokens") Integer maxTokens
) {
}
