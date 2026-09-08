package com.teamreports.weeklyreport.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamreports.weeklyreport.ai.AnthropicClient;
import com.teamreports.weeklyreport.ai.AnthropicMessage;
import com.teamreports.weeklyreport.ai.AnthropicRequest;
import com.teamreports.weeklyreport.ai.AnthropicResponse;
import com.teamreports.weeklyreport.ai.AnthropicTool;
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

/**
 * Conversational assistant for managers (assignment section 8, "Good to have").
 *
 * Design: rather than dumping the whole team's report history into the prompt (expensive,
 * and an easy way to leak more than the question needs), the model is given two narrow
 * read-only tools backed by {@link ReportQueryTools} and decides for itself which one(s)
 * to call to answer a given question - genuine tool use / function calling, not a fixed
 * RAG blob. The loop below is intentionally small and bounded (max-tool-iterations) so a
 * confused model can't spin forever.
 *
 * Data-privacy: only report content a manager can already see in the dashboard is ever
 * placed in a prompt (see the note on {@link ReportQueryTools}). No data is persisted by
 * this service beyond the lifetime of a single request; nothing is sent anywhere except
 * to the configured Anthropic endpoint.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

    private static final String WEEK_SNAPSHOT_TOOL = "get_week_snapshot";
    private static final String MEMBER_HISTORY_TOOL = "get_member_history";

    private final AnthropicClient anthropicClient;
    private final AiProperties aiProperties;
    private final ReportQueryTools reportQueryTools;
    private final ObjectMapper objectMapper;

    public String ask(String question) {
        if (!aiProperties.isEnabledAndConfigured()) {
            throw new AiUnavailableException(
                    "The AI assistant is not configured. Ask an administrator to enable it.");
        }

        List<AnthropicMessage> messages = new ArrayList<>();
        messages.add(AnthropicMessage.user(question));

        for (int iteration = 0; iteration < aiProperties.maxToolIterations(); iteration++) {
            AnthropicResponse response = anthropicClient.createMessage(new AnthropicRequest(
                    aiProperties.model(), aiProperties.maxOutputTokens(), systemPrompt(), messages, tools()));

            if (!"tool_use".equals(response.stopReason())) {
                return extractText(response);
            }

            // The assistant asked to call one or more tools: run them and feed the results back.
            messages.add(new AnthropicMessage("assistant", response.content()));
            messages.add(new AnthropicMessage("user", executeToolCalls(response.content())));
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

    private List<AnthropicTool> tools() {
        return List.of(
                new AnthropicTool(WEEK_SNAPSHOT_TOOL,
                        "Get every team member's report content for one specific week - use this for "
                                + "'what did the team work on' / 'who has blockers' style questions.",
                        Map.of(
                                "type", "object",
                                "properties", Map.of(
                                        "weekStartDate", Map.of(
                                                "type", "string",
                                                "description", "Monday of the target week, format yyyy-MM-dd")),
                                "required", List.of("weekStartDate"))),
                new AnthropicTool(MEMBER_HISTORY_TOOL,
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

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> executeToolCalls(List<Map<String, Object>> assistantContent) {
        List<Map<String, Object>> results = new ArrayList<>();

        for (Map<String, Object> block : assistantContent) {
            if (!"tool_use".equals(block.get("type"))) {
                continue;
            }
            String toolUseId = (String) block.get("id");
            String toolName = (String) block.get("name");
            Map<String, Object> input = (Map<String, Object>) block.getOrDefault("input", Map.of());

            Object toolResult = runTool(toolName, input);
            results.add(Map.of(
                    "type", "tool_result",
                    "tool_use_id", toolUseId,
                    "content", toJson(toolResult)));
        }
        return results;
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

    @SuppressWarnings("unchecked")
    private String extractText(AnthropicResponse response) {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> block : response.content()) {
            if ("text".equals(block.get("type"))) {
                sb.append((String) block.get("text"));
            }
        }
        if (sb.isEmpty()) {
            throw new AiServiceException("The assistant returned an empty response.");
        }
        return sb.toString();
    }
}
