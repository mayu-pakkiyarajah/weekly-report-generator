package com.teamreports.weeklyreport.controller;

import com.teamreports.weeklyreport.dto.chat.ChatRequest;
import com.teamreports.weeklyreport.dto.chat.ChatResponse;
import com.teamreports.weeklyreport.dto.chat.TeamSummaryResponse;
import com.teamreports.weeklyreport.service.ai.AiChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Optional AI chat assistant (assignment section 8, "Good to have").
 * Manager-only, same as the rest of the dashboard - the assistant only ever
 * summarizes data a manager could already see by clicking through reports themselves.
 */
@RestController
@RequestMapping("/api/manager/chat")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerChatController {

    private final AiChatService aiChatService;

    @PostMapping("/ask")
    public ChatResponse ask(@Valid @RequestBody ChatRequest request) {
        return new ChatResponse(aiChatService.ask(request.question()));
    }

    @GetMapping("/summary")
    public TeamSummaryResponse summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate weekStart) {
        String summary = aiChatService.ask(aiChatService.buildWeeklySummaryPrompt(weekStart));
        return new TeamSummaryResponse(weekStart, summary);
    }
}
