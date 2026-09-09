package com.teamreports.weeklyreport.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamreports.weeklyreport.ai.OpenAiClient;
import com.teamreports.weeklyreport.ai.OpenAiMessage;
import com.teamreports.weeklyreport.ai.OpenAiRequest;
import com.teamreports.weeklyreport.ai.OpenAiResponse;
import com.teamreports.weeklyreport.ai.OpenAiTool;
import com.teamreports.weeklyreport.config.AiProperties;
import com.teamreports.weeklyreport.exception.AiServiceException;
import com.teamreports.weeklyreport.exception.AiUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// After fixing with OpenAI api

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

    private static final String WEEK_SNAPSHOT_TOOL = "get_week_snapshot";
    private static final String MEMBER_HISTORY_TOOL = "get_member_history";

    private final OpenAiClient openAiClient;
    private final AiProperties aiProperties;
    private final ReportQueryTools reportQueryTools;
    private final ObjectMapper objectMapper;

    public String ask(String question) {
        if (!aiProperties.isEnabledAndConfigured()) {
            throw new AiUnavailableException(
                    "The AI assistant is not configured. Ask an administrator to enable it.");
        }

        List<OpenAiMessage> messages = new ArrayList<>();
        messages.add(OpenAiMessage.system(systemPrompt()));
        messages.add(OpenAiMessage.user(question));

        for (int iteration = 0; iteration < aiProperties.maxToolIterations(); iteration++) {
            OpenAiResponse response = openAiClient.createChatCompletion(new OpenAiRequest(
                    aiProperties.model(), messages, tools(), "auto", aiProperties.maxOutputTokens()));

            OpenAiResponse.Choice choice = firstChoice(response);

            if (!"tool_calls".equals(choice.finishReason())) {
                return extractText(choice);
            }

            // The assistant asked to call one or more tools: run them and feed the results back.
            List<Map<String, Object>> toolCalls = choice.message().toolCalls();
            messages.add(OpenAiMessage.assistantWithToolCalls(choice.message().content(), toolCalls));
            for (OpenAiMessage toolResult : executeToolCalls(toolCalls)) {
                messages.add(toolResult);
            }
        }

        throw new AiServiceException("The assistant needed too many steps to answer this question.");
    }

    public String buildWeeklySummaryPrompt(LocalDate weekStart) {
        return "Write a concise weekly summary for the manager, for the week starting " + weekStart + ". "
                + "First call " + WEEK_SNAPSHOT_TOOL + " for that week to get real data - do not guess. "
                + "Structure the summary in three short sections with plain headings (no markdown headers, "
                + "just a bolded label per section is fine): what the team completed, recurring or "
                + "notable blockers, and any workload imbalance you notice across team members. "
                + "Keep it under 200 words and only state things the data actually supports.";
    }

    // ---------- system prompt & tool definitions ----------

    private String systemPrompt() {
        return "You are an assistant embedded in a team's weekly-report tool, answering a manager's "
                + "questions about their team's reported work. Today's date is " + LocalDate.now() + ". "
                + "Always use the provided tools to fetch real report data before answering factual "
                + "questions - never invent tasks, blockers, or names. If a tool returns no data for what "
                + "was asked, say so plainly rather than guessing. Keep answers concise and specific, "
                + "referencing team member and project names from the tool results.";
    }

    private List<OpenAiTool> tools() {
        return List.of(
                OpenAiTool.function(WEEK_SNAPSHOT_TOOL,
                        "Get every team member's report content for one specific week - use this for "
                                + "'what did the team work on' / 'who has blockers' style questions.",
                        Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "weekStartDate", Map.of(
                                                "type", "string",
                                                "description", "Monday of the target week, format yyyy-MM-dd")),
                                "required", List.of("weekStartDate"))),
                OpenAiTool.function(MEMBER_HISTORY_TOOL,
                        "Get one team member's reports over their last N weeks - use this for "
                                + "'what has <person> been working on' style questions.",
                        Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "memberName", Map.of(
                                                "type", "string",
                                                "description", "Full or partial name of the team member"),
                                        "weeksBack", Map.of(
                                                "type", "integer",
                                                "description", "How many weeks back to look, default 4")),
                                "required", List.of("memberName"))));
    }

    // ---------- tool execution ----------

    private List<OpenAiMessage> executeToolCalls(List<Map<String, Object>> toolCalls) {
        List<OpenAiMessage> results = new ArrayList<>();

        for (Map<String, Object> call : toolCalls) {
            String callId = (String) call.get("id");
            @SuppressWarnings("unchecked")
            Map<String, Object> function = (Map<String, Object>) call.get("function");
            String toolName = (String) function.get("name");
            String argumentsJson = (String) function.get("arguments");

            Map<String, Object> input = parseArguments(argumentsJson);
            Object toolResult = runTool(toolName, input);
            results.add(OpenAiMessage.toolResult(callId, toJson(toolResult)));
        }
        return results;
    }

    private Map<String, Object> parseArguments(String argumentsJson) {
        if (argumentsJson == null || argumentsJson.isBlank()) {
            return Map.of();
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = objectMapper.readValue(argumentsJson, Map.class);
            return parsed;
        } catch (Exception ex) {
            log.warn("Could not parse tool call arguments '{}': {}", argumentsJson, ex.getMessage());
            return Map.of();
        }
    }

    private Object runTool(String toolName, Map<String, Object> input) {
        try {
            return switch (toolName) {
                case WEEK_SNAPSHOT_TOOL -> reportQueryTools.weekSnapshot(
                        LocalDate.parse((String) input.get("weekStartDate")));
                case MEMBER_HISTORY_TOOL -> reportQueryTools.memberHistory(
                        (String) input.get("memberName"),
                        input.get("weeksBack") instanceof Number n ? n.intValue() : 4);
                default -> Map.of("error", "Unknown tool: " + toolName);
            };
        } catch (Exception ex) {
            log.warn("Tool execution failed for {}: {}", toolName, ex.getMessage());
            return Map.of("error", "Could not run this lookup: " + ex.getMessage());
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private OpenAiResponse.Choice firstChoice(OpenAiResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new AiServiceException("The assistant returned an empty response.");
        }
        return response.choices().get(0);
    }

    private String extractText(OpenAiResponse.Choice choice) {
        String content = choice.message().content();
        if (content == null || content.isBlank()) {
            throw new AiServiceException("The assistant returned an empty response.");
        }
        return content;
    }
}
