package com.teamreports.weeklyreport.dto.dashboard;

import java.util.List;

/** Bonus feature: one section (e.g. Blockers) across every team member for a given week. */
public record SectionAcrossTeamResponse(Long userId, String userFullName, List<String> entries) {
}
