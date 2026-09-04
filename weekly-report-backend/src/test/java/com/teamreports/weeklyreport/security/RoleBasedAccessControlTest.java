package com.teamreports.weeklyreport.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamreports.weeklyreport.entity.User;
import com.teamreports.weeklyreport.entity.enums.Role;
import com.teamreports.weeklyreport.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end tests for the role-based / ownership-based access control rules that the
 * assignment explicitly calls out as critical:
 *   - a team member must never be able to read or write another team member's report
 *   - a team member must never be able to reach a manager-only endpoint
 *   - a manager can reach manager-only endpoints
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoleBasedAccessControlTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        // Bootstrap a manager directly (the public /api/auth/register endpoint deliberately
        // can only create TEAM_MEMBER accounts - see AuthService).
        userRepository.save(User.builder()
                .fullName("Test Manager")
                .email("manager@test.local")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.MANAGER)
                .active(true)
                .build());
    }

    @Test
    void teamMemberCannotAccessManagerOnlyEndpoint() throws Exception {
        String memberToken = registerAndLogin("member-a@test.local", "Member A");

        mockMvc.perform(get("/api/manager/reports")
                        .header("Authorization", "Bearer " + memberToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void managerCanAccessManagerOnlyEndpoint() throws Exception {
        String managerToken = login("manager@test.local", "Password123!");

        mockMvc.perform(get("/api/manager/reports")
                        .header("Authorization", "Bearer " + managerToken))
                .andExpect(status().isOk());
    }

    @Test
    void teamMemberCannotReadAnotherTeamMembersReport() throws Exception {
        String userAToken = registerAndLogin("member-b@test.local", "Member B");
        String userBToken = registerAndLogin("member-c@test.local", "Member C");

        // Need an active project to attach the report to.
        String managerToken = login("manager@test.local", "Password123!");
        String projectBody = objectMapper.writeValueAsString(Map.of(
                "name", "RBAC Test Project", "description", "used only by the RBAC test"));

        String projectResponse = mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(projectBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long projectId = ((Number) objectMapper.readValue(projectResponse, Map.class).get("id")).longValue();

        // User A creates their own draft report.
        String reportBody = objectMapper.writeValueAsString(Map.of(
                "weekStartDate", "2025-01-06",
                "weekEndDate", "2025-01-12",
                "projectId", projectId));

        String reportResponse = mockMvc.perform(post("/api/reports")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reportBody))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long reportId = ((Number) objectMapper.readValue(reportResponse, Map.class).get("id")).longValue();

        // User A can read their own report.
        mockMvc.perform(get("/api/reports/" + reportId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk());

        // User B must be denied access to User A's report through the team-member endpoint.
        mockMvc.perform(get("/api/reports/" + reportId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void requestWithoutTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().is4xxClientError());
    }

    private String registerAndLogin(String email, String fullName) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "fullName", fullName, "email", email, "password", "Password123!"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        return login(email, "Password123!");
    }

    private String login(String email, String password) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", email, "password", password));

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        return (String) objectMapper.readValue(response, Map.class).get("accessToken");
    }
}
