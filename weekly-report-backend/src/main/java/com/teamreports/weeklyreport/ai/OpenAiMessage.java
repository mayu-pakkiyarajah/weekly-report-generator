package com.teamreports.weeklyreport.ai;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * A single turn in the conversation sent to the OpenAI Chat Completions API.
 * Unlike Anthropic's Messages API, OpenAI has no separate top-level "system" field -
 * the system prompt is just the first message with role "system". Tool calls and tool
 * results are also modeled as distinct message shapes (role "assistant" with
 * {@code toolCalls}, or role "tool" with {@code toolCallId}), so most fields here are
 * optional and omitted from the JSON when null.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record OpenAiMessage(
        String role,
        Object content,
        @JsonProperty("tool_calls") List<Map<String, Object>> toolCalls,
        @JsonProperty("tool_call_id") String toolCallId
) {
    public static OpenAiMessage system(String text) {
        return new OpenAiMessage("system", text, null, null);
    }

    public static OpenAiMessage user(String text) {
        return new OpenAiMessage("user", text, null, null);
    }

    public static OpenAiMessage assistantWithToolCalls(String content, List<Map<String, Object>> toolCalls) {
        return new OpenAiMessage("assistant", content, toolCalls, null);
    }

    public static OpenAiMessage toolResult(String toolCallId, String content) {
        return new OpenAiMessage("tool", content, null, toolCallId);
    }
}
