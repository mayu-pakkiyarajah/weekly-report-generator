package com.teamreports.weeklyreport.dto.chat;

import java.time.LocalDate;

public record TeamSummaryResponse(LocalDate weekStartDate, String summary) {
}
