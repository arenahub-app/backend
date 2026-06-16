package com.arenahub.presentation.match;

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
class MatchControllerIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EmailSenderPort emailSenderPort;

    private String accessToken;
    private String groupId;

    @BeforeEach
    void setUp() throws Exception {
        Mockito.reset(emailSenderPort);
        String email = "match-" + UUID.randomUUID() + "@test.com";

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Match Tester",
                                  "email": "%s",
                                  "password": "Senha@123",
                                  "phone": "11900000001",
                                  "birthDate": "1990-01-01"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort).sendEmailVerification(eq(email), urlCaptor.capture());
        String verifyToken = urlCaptor.getValue().replaceAll(".*token=", "");

        mvc.perform(get("/api/v1/auth/verify-email").param("token", verifyToken))
                .andExpect(status().isNoContent());

        MvcResult loginResult = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "Senha@123"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        accessToken = loginJson.get("accessToken").asText();

        MvcResult groupResult = mvc.perform(post("/api/v1/groups")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Test Group", "sport": "FOOTBALL"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        groupId = objectMapper.readTree(groupResult.getResponse().getContentAsString())
                .get("id").asText();
    }

    private String futureDate() {
        return Instant.now().plus(2, ChronoUnit.DAYS).toString();
    }

    @Test
    void createMatch_returns201WithMatchData() throws Exception {
        mvc.perform(post("/api/v1/groups/{groupId}/matches", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scheduledAt": "%s",
                                  "locationName": "Quadra Central",
                                  "locationAddress": "Av. 1, 100",
                                  "maxPlayers": 10
                                }
                                """.formatted(futureDate())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.presenceListStatus").value("OPEN"))
                .andExpect(jsonPath("$.maxPlayers").value(10))
                .andExpect(jsonPath("$.locationName").value("Quadra Central"));
    }

    @Test
    void listMatches_returnsEmptyList_initially() throws Exception {
        mvc.perform(get("/api/v1/groups/{groupId}/matches", groupId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void fullPresenceFlow_confirmAndCancel() throws Exception {
        MvcResult createResult = mvc.perform(post("/api/v1/groups/{groupId}/matches", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scheduledAt": "%s",
                                  "locationName": "Quadra",
                                  "maxPlayers": 10
                                }
                                """.formatted(futureDate())))
                .andExpect(status().isCreated())
                .andReturn();

        String matchId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/presence", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\": \"CONFIRM\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("PRESENCE"));

        mvc.perform(get("/api/v1/groups/{groupId}/matches/{matchId}/presence", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmed", hasSize(1)));

        mvc.perform(delete("/api/v1/groups/{groupId}/matches/{matchId}/presence", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/groups/{groupId}/matches/{matchId}/presence", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmed", hasSize(0)));
    }

    @Test
    void createMatch_returns400_whenScheduledAtTooSoon() throws Exception {
        String tooSoon = Instant.now().plus(5, ChronoUnit.MINUTES).toString();

        mvc.perform(post("/api/v1/groups/{groupId}/matches", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scheduledAt": "%s",
                                  "locationName": "Quadra",
                                  "maxPlayers": 10
                                }
                                """.formatted(tooSoon)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelMatch_returns204() throws Exception {
        MvcResult createResult = mvc.perform(post("/api/v1/groups/{groupId}/matches", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scheduledAt": "%s",
                                  "locationName": "Quadra",
                                  "maxPlayers": 10
                                }
                                """.formatted(futureDate())))
                .andExpect(status().isCreated())
                .andReturn();

        String matchId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/cancel", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/groups/{groupId}/matches/{matchId}", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void confirmPresence_returns422_whenListClosed() throws Exception {
        MvcResult createResult = mvc.perform(post("/api/v1/groups/{groupId}/matches", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scheduledAt": "%s",
                                  "locationName": "Quadra",
                                  "maxPlayers": 10
                                }
                                """.formatted(futureDate())))
                .andExpect(status().isCreated())
                .andReturn();

        String matchId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/close-list", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/presence", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\": \"CONFIRM\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value(containsString("list-closed")));
    }

    @Test
    void banMember_returns403_whenListClosed_andMemberTriesToConfirm() throws Exception {
        mvc.perform(post("/api/v1/groups/{groupId}/matches", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scheduledAt": "%s",
                                  "locationName": "Quadra",
                                  "maxPlayers": 10
                                }
                                """.formatted(futureDate())))
                .andExpect(status().isCreated());
    }
}
