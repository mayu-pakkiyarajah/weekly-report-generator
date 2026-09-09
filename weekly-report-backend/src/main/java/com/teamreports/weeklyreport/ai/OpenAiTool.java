package com.teamreports.weeklyreport.ai;

import java.util.Map;

/** A tool definition in the shape OpenAI's Chat Completions API expects. */
public record OpenAiTool(String type, OpenAiFunction function) {
    public static OpenAiTool function(String name, String description, Map<String, Object> parameters) {
        return new OpenAiTool("function", new OpenAiFunction(name, description, parameters));
    }

    public record OpenAiFunction(String name, String description, Map<String, Object> parameters) {
    }
}
