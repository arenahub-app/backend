package com.arenahub.presentation.teamformation;

import com.arenahub.application.auth.port.out.EmailSenderPort;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class TeamFormationControllerIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EmailSenderPort emailSenderPort;

    private String ownerToken;
    private String player2Token;
    private String groupId;
    private String matchId;
    private String ownerMemberId;
    private String player2MemberId;

    @BeforeEach
    void setUp() throws Exception {
        Mockito.reset(emailSenderPort);

        String ownerEmail = "tf-owner-" + UUID.randomUUID() + "@test.com";
        String player2Email = "tf-p2-" + UUID.randomUUID() + "@test.com";

        ownerToken = registerAndLogin(ownerEmail, "Owner");
        player2Token = registerAndLogin(player2Email, "Player2");

        MvcResult groupResult = mvc.perform(post("/api/v1/groups")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "TF Group", "sport": "FOOTBALL"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        groupId = objectMapper.readTree(groupResult.getResponse().getContentAsString()).get("id").asText();

        MvcResult inviteResult = mvc.perform(post("/api/v1/groups/{groupId}/invites", groupId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isCreated())
                .andReturn();
        String inviteCode = objectMapper.readTree(inviteResult.getResponse().getContentAsString()).get("code").asText();

        mvc.perform(post("/api/v1/groups/invites/{code}/join", inviteCode)
                        .header("Authorization", "Bearer " + player2Token))
                .andExpect(status().isOk());

        MvcResult membersResult = mvc.perform(get("/api/v1/groups/{groupId}/members", groupId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode members = objectMapper.readTree(membersResult.getResponse().getContentAsString());
        for (JsonNode member : members) {
            String role = member.get("role").asText();
            String userId = member.get("userId").asText();
            if ("OWNER".equals(role)) {
                ownerMemberId = member.get("id").asText();
            } else {
                player2MemberId = member.get("id").asText();
            }
        }

        String futureDate = Instant.now().plus(2, ChronoUnit.DAYS).toString();
        MvcResult matchResult = mvc.perform(post("/api/v1/groups/{groupId}/matches", groupId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scheduledAt": "%s",
                                  "locationName": "Quadra TF",
                                  "maxPlayers": 10
                                }
                                """.formatted(futureDate)))
                .andExpect(status().isCreated())
                .andReturn();
        matchId = objectMapper.readTree(matchResult.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/presence", groupId, matchId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\": \"CONFIRM\"}"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/presence", groupId, matchId)
                        .header("Authorization", "Bearer " + player2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\": \"CONFIRM\"}"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/close-list", groupId, matchId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());
    }

    private String registerAndLogin(String email, String name) throws Exception {
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "email": "%s",
                                  "password": "Senha@123",
                                  "phone": "11900000001",
                                  "birthDate": "1990-01-01"
                                }
                                """.formatted(name, email)))
                .andExpect(status().isCreated());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort).sendEmailVerification(eq(email), urlCaptor.capture());
        String verifyToken = urlCaptor.getValue().replaceAll(".*token=", "");
        Mockito.reset(emailSenderPort);

        mvc.perform(get("/api/v1/auth/verify-email").param("token", verifyToken))
                .andExpect(status().isNoContent());

        MvcResult loginResult = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "Senha@123"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("accessToken").asText();
    }

    @Test
    void generateFormation_withClosedList_returns201() throws Exception {
        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/team-formations", groupId, matchId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numberOfTeams\": 2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.numberOfTeams").value(2))
                .andExpect(jsonPath("$.formationType").value("AUTOMATIC"))
                .andExpect(jsonPath("$.teams").isArray())
                .andExpect(jsonPath("$.teams", hasSize(2)));
    }

    @Test
    void getCurrentFormation_returns200WithTeams() throws Exception {
        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/team-formations", groupId, matchId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numberOfTeams\": 2}"))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/groups/{groupId}/matches/{matchId}/team-formations/current", groupId, matchId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberOfTeams").value(2))
                .andExpect(jsonPath("$.teams", hasSize(2)));
    }

    @Test
    void generateFormation_asPlayer_returns403() throws Exception {
        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/team-formations", groupId, matchId)
                        .header("Authorization", "Bearer " + player2Token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numberOfTeams\": 2}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void generateFormation_withOpenList_returns422() throws Exception {
        String futureDate = Instant.now().plus(3, ChronoUnit.DAYS).toString();
        MvcResult matchResult = mvc.perform(post("/api/v1/groups/{groupId}/matches", groupId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scheduledAt": "%s",
                                  "locationName": "Quadra Open",
                                  "maxPlayers": 10
                                }
                                """.formatted(futureDate)))
                .andExpect(status().isCreated())
                .andReturn();
        String openMatchId = objectMapper.readTree(matchResult.getResponse().getContentAsString()).get("id").asText();

        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/team-formations", groupId, openMatchId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"numberOfTeams\": 2}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value(containsString("presence-list-not-closed")));
    }

    @Test
    void movePlayer_returns200WithManualAdjustedType() throws Exception {
        MvcResult genResult = mvc.perform(
                        post("/api/v1/groups/{groupId}/matches/{matchId}/team-formations", groupId, matchId)
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"numberOfTeams\": 2}"))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode formation = objectMapper.readTree(genResult.getResponse().getContentAsString());
        String formationId = formation.get("id").asText();

        JsonNode teamA = formation.get("teams").get(0);
        JsonNode teamB = formation.get("teams").get(1);
        String teamAId = teamA.get("id").asText();
        String teamBId = teamB.get("id").asText();

        String memberToMove = null;
        if (teamA.get("players").size() > 0) {
            memberToMove = teamA.get("players").get(0).get("memberId").asText();
        }

        if (memberToMove == null) {
            return;
        }

        mvc.perform(put("/api/v1/groups/{groupId}/matches/{matchId}/team-formations/{formationId}/move",
                        groupId, matchId, formationId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"memberId": "%s", "toTeamId": "%s"}
                                """.formatted(memberToMove, teamBId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.formationType").value("MANUAL_ADJUSTED"));
    }

    @Test
    void deleteFormation_returns204() throws Exception {
        MvcResult genResult = mvc.perform(
                        post("/api/v1/groups/{groupId}/matches/{matchId}/team-formations", groupId, matchId)
                                .header("Authorization", "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"numberOfTeams\": 2}"))
                .andExpect(status().isCreated())
                .andReturn();

        String formationId = objectMapper.readTree(genResult.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(delete("/api/v1/groups/{groupId}/matches/{matchId}/team-formations/{formationId}",
                        groupId, matchId, formationId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/groups/{groupId}/matches/{matchId}/team-formations/current",
                        groupId, matchId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNotFound());
    }
}
