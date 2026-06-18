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
class GuestControllerIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EmailSenderPort emailSenderPort;

    private String accessToken;
    private String groupId;
    private String matchId;

    @BeforeEach
    void setUp() throws Exception {
        Mockito.reset(emailSenderPort);
        String email = "guest-it-" + UUID.randomUUID() + "@test.com";

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Guest Tester",
                                  "email": "%s",
                                  "password": "Senha@123",
                                  "phone": "11900000002",
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
                                {"name": "Guest Test Group", "sport": "FOOTBALL"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        groupId = objectMapper.readTree(groupResult.getResponse().getContentAsString())
                .get("id").asText();

        MvcResult matchResult = mvc.perform(post("/api/v1/groups/{groupId}/matches", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "scheduledAt": "%s",
                                  "locationName": "Quadra Convidados",
                                  "maxPlayers": 2
                                }
                                """.formatted(Instant.now().plus(2, ChronoUnit.DAYS).toString())))
                .andExpect(status().isCreated())
                .andReturn();

        matchId = objectMapper.readTree(matchResult.getResponse().getContentAsString())
                .get("id").asText();
    }

    @Test
    void addGuest_withoutFee_returnsConfirmedGuest() throws Exception {
        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/guests", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "João Convidado",
                                  "skill": 4.0,
                                  "position": "FORWARD"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("João Convidado"))
                .andExpect(jsonPath("$.skill").value(4.0))
                .andExpect(jsonPath("$.position").value("FORWARD"))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.chargeId").doesNotExist());
    }

    @Test
    void addGuest_withFee_returnsPaymentPendingAndCreatesCharge() throws Exception {
        mvc.perform(patch("/api/v1/groups/{groupId}", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Guest Test Group", "matchFee": 30.00}
                                """))
                .andExpect(status().isOk());

        MvcResult result = mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/guests", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Pedro Convidado",
                                  "skill": 3.5,
                                  "position": "MIDFIELDER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PAYMENT_PENDING"))
                .andExpect(jsonPath("$.chargeId").isNotEmpty())
                .andReturn();

        String guestId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(get("/api/v1/groups/{groupId}/matches/{matchId}/presence", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmed[?(@.guestId == '%s')].type".formatted(guestId),
                        hasItem("GUEST")));
    }

    @Test
    void addGuest_whenMatchFull_returns422() throws Exception {
        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/presence", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"action\": \"CONFIRM\"}"))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/guests", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Convidado Extra",
                                  "skill": 3.0,
                                  "position": "DEFENDER"
                                }
                                """))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/guests", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Convidado Excedente",
                                  "skill": 3.0,
                                  "position": "DEFENDER"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.type").value(containsString("match-full")));
    }

    @Test
    void removeGuest_returns204() throws Exception {
        MvcResult addResult = mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/guests", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Convidado Removível",
                                  "skill": 2.5,
                                  "position": "GOALKEEPER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String guestId = objectMapper.readTree(addResult.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(delete("/api/v1/groups/{groupId}/matches/{matchId}/guests/{guestId}",
                        groupId, matchId, guestId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mvc.perform(get("/api/v1/groups/{groupId}/matches/{matchId}/presence", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmed", hasSize(0)));
    }

    @Test
    void addGuest_missingSkill_returns400() throws Exception {
        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/guests", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Sem Skill",
                                  "position": "FORWARD"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addGuest_invalidSkillRange_returns400() throws Exception {
        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/guests", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Skill Inválida",
                                  "skill": 6.0,
                                  "position": "FORWARD"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void guestAppearsInPresenceList() throws Exception {
        mvc.perform(post("/api/v1/groups/{groupId}/matches/{matchId}/guests", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Convidado Lista",
                                  "skill": 4.5,
                                  "position": "LATERAL"
                                }
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/groups/{groupId}/matches/{matchId}/presence", groupId, matchId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmed", hasSize(1)))
                .andExpect(jsonPath("$.confirmed[0].type").value("GUEST"))
                .andExpect(jsonPath("$.confirmed[0].userName").value("Convidado Lista"))
                .andExpect(jsonPath("$.confirmed[0].skill").value(4.5));
    }
}
