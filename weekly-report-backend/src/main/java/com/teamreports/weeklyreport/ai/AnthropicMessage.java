package com.teamreports.weeklyreport.ai;

/**
 * A single turn in the conversation sent to the Anthropic Messages API.
 * {@code content} is intentionally {@code Object}: it's a plain String for simple text
 * turns, or a List<Map<String,Object>> of content blocks (text / tool_use / tool_result)
 * once tool calls enter the conversation - the Anthropic API itself allows both shapes.
 */
public record AnthropicMessage(String role, Object content) {
    public static AnthropicMessage user(String text) {
        return new AnthropicMessage("user", text);
    }
}
