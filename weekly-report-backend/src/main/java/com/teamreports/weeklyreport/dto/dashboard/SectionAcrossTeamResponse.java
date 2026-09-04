package com.teamreports.weeklyreport.dto.dashboard;

import java.util.List;

public record SectionAcrossTeamResponse(Long userId, String userFullName, List<String> entries) {
}
