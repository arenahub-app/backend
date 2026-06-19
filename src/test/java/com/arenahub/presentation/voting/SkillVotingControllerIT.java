package com.arenahub.presentation.voting;

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

import static org.assertj.core.api.Assertions.assertThat;
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
class SkillVotingControllerIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EmailSenderPort emailSenderPort;

    private String accessToken;
    private String groupId;
    private String memberId;

    @BeforeEach
    void setUp() throws Exception {
        Mockito.reset(emailSenderPort);
        String email = "voting-" + UUID.randomUUID() + "@test.com";

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Voting Tester",
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
                                {"name": "Voting Group", "sport": "FOOTBALL"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode groupJson = objectMapper.readTree(groupResult.getResponse().getContentAsString());
        groupId = groupJson.get("id").asText();

        MvcResult membersResult = mvc.perform(get("/api/v1/groups/{groupId}/members", groupId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andReturn();
        memberId = objectMapper.readTree(membersResult.getResponse().getContentAsString())
                .get(0).get("id").asText();
    }

    private String futureDeadline() {
        return Instant.now().plus(2, ChronoUnit.HOURS).toString();
    }

    @Test
    void openVoting_returns201() throws Exception {
        mvc.perform(post("/api/v1/groups/{groupId}/votings", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deadline": "%s"}
                                """.formatted(futureDeadline())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.groupId").value(groupId));
    }

    @Test
    void openVoting_returns409_whenAlreadyActive() throws Exception {
        mvc.perform(post("/api/v1/groups/{groupId}/votings", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deadline": "%s"}
                                """.formatted(futureDeadline())))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/v1/groups/{groupId}/votings", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deadline": "%s"}
                                """.formatted(futureDeadline())))
                .andExpect(status().isConflict());
    }

    @Test
    void getActiveVoting_returns200() throws Exception {
        mvc.perform(post("/api/v1/groups/{groupId}/votings", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deadline": "%s"}
                                """.formatted(futureDeadline())))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/groups/{groupId}/votings/active", groupId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void getActiveVoting_returns404_whenNone() throws Exception {
        mvc.perform(get("/api/v1/groups/{groupId}/votings/active", groupId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void listVotings_returnsEmptyInitially() throws Exception {
        mvc.perform(get("/api/v1/groups/{groupId}/votings", groupId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void closeVoting_returns200WithResults() throws Exception {
        MvcResult openResult = mvc.perform(post("/api/v1/groups/{groupId}/votings", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deadline": "%s"}
                                """.formatted(futureDeadline())))
                .andExpect(status().isCreated())
                .andReturn();

        String votingId = objectMapper.readTree(openResult.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(post("/api/v1/groups/{groupId}/votings/{votingId}/close", groupId, votingId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.closedAt").isNotEmpty());
    }

    @Test
    void closeVoting_returns422_whenAlreadyClosed() throws Exception {
        MvcResult openResult = mvc.perform(post("/api/v1/groups/{groupId}/votings", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deadline": "%s"}
                                """.formatted(futureDeadline())))
                .andExpect(status().isCreated())
                .andReturn();

        String votingId = objectMapper.readTree(openResult.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(post("/api/v1/groups/{groupId}/votings/{votingId}/close", groupId, votingId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        mvc.perform(post("/api/v1/groups/{groupId}/votings/{votingId}/close", groupId, votingId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void fullVotingFlow_openVoteAndClose() throws Exception {
        String email2 = "voter-" + UUID.randomUUID() + "@test.com";
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Second Voter",
                                  "email": "%s",
                                  "password": "Senha@123",
                                  "phone": "11900000002",
                                  "birthDate": "1991-01-01"
                                }
                                """.formatted(email2)))
                .andExpect(status().isCreated());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort, Mockito.atLeastOnce()).sendEmailVerification(eq(email2), urlCaptor.capture());
        String verifyToken2 = urlCaptor.getAllValues().stream()
                .filter(v -> v.contains(email2) || urlCaptor.getAllValues().size() == 1)
                .findFirst().orElseGet(() -> urlCaptor.getValue())
                .replaceAll(".*token=", "");
        mvc.perform(get("/api/v1/auth/verify-email").param("token", verifyToken2))
                .andExpect(status().isNoContent());

        MvcResult login2 = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "Senha@123"}
                                """.formatted(email2)))
                .andExpect(status().isOk())
                .andReturn();
        String token2 = objectMapper.readTree(login2.getResponse().getContentAsString())
                .get("accessToken").asText();

        MvcResult inviteResult = mvc.perform(post("/api/v1/groups/{groupId}/invites", groupId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();
        String inviteToken = objectMapper.readTree(inviteResult.getResponse().getContentAsString())
                .get("token").asText();

        MvcResult joinResult = mvc.perform(post("/api/v1/invites/{token}/join", inviteToken)
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isOk())
                .andReturn();
        String member2Id = objectMapper.readTree(joinResult.getResponse().getContentAsString())
                .get("memberId").asText();

        MvcResult openResult = mvc.perform(post("/api/v1/groups/{groupId}/votings", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deadline": "%s"}
                                """.formatted(futureDeadline())))
                .andExpect(status().isCreated())
                .andReturn();

        String votingId = objectMapper.readTree(openResult.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(put("/api/v1/groups/{groupId}/votings/{votingId}/votes/{targetMemberId}",
                        groupId, votingId, member2Id)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stars\": 5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stars").value(5));

        mvc.perform(get("/api/v1/groups/{groupId}/votings/{votingId}/votes/my", groupId, votingId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.votes", hasSize(1)))
                .andExpect(jsonPath("$.votes[0].stars").value(5));

        mvc.perform(post("/api/v1/groups/{groupId}/votings/{votingId}/close", groupId, votingId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        mvc.perform(get("/api/v1/groups/{groupId}/votings/{votingId}/results", groupId, votingId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void castVote_returns422_selfVote() throws Exception {
        MvcResult openResult = mvc.perform(post("/api/v1/groups/{groupId}/votings", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deadline": "%s"}
                                """.formatted(futureDeadline())))
                .andExpect(status().isCreated())
                .andReturn();

        String votingId = objectMapper.readTree(openResult.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(put("/api/v1/groups/{groupId}/votings/{votingId}/votes/{targetMemberId}",
                        groupId, votingId, memberId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"stars\": 4}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void banAndUnban_flow() throws Exception {
        String email3 = "ban-" + UUID.randomUUID() + "@test.com";
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Ban Target",
                                  "email": "%s",
                                  "password": "Senha@123",
                                  "phone": "11900000003",
                                  "birthDate": "1992-01-01"
                                }
                                """.formatted(email3)))
                .andExpect(status().isCreated());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort, Mockito.atLeastOnce()).sendEmailVerification(eq(email3), urlCaptor.capture());
        String verifyToken3 = urlCaptor.getValue().replaceAll(".*token=", "");
        mvc.perform(get("/api/v1/auth/verify-email").param("token", verifyToken3))
                .andExpect(status().isNoContent());

        MvcResult login3 = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "Senha@123"}
                                """.formatted(email3)))
                .andExpect(status().isOk())
                .andReturn();
        String token3 = objectMapper.readTree(login3.getResponse().getContentAsString())
                .get("accessToken").asText();

        MvcResult inviteResult = mvc.perform(post("/api/v1/groups/{groupId}/invites", groupId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();
        String inviteToken = objectMapper.readTree(inviteResult.getResponse().getContentAsString())
                .get("token").asText();

        MvcResult joinResult = mvc.perform(post("/api/v1/invites/{token}/join", inviteToken)
                        .header("Authorization", "Bearer " + token3))
                .andExpect(status().isOk())
                .andReturn();
        String banMemberId = objectMapper.readTree(joinResult.getResponse().getContentAsString())
                .get("memberId").asText();

        MvcResult openResult = mvc.perform(post("/api/v1/groups/{groupId}/votings", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"deadline": "%s"}
                                """.formatted(futureDeadline())))
                .andExpect(status().isCreated())
                .andReturn();
        String votingId = objectMapper.readTree(openResult.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(post("/api/v1/groups/{groupId}/votings/{votingId}/bans", groupId, votingId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"memberId": "%s", "reason": "Motivo de teste"}
                                """.formatted(banMemberId)))
                .andExpect(status().isCreated());

        mvc.perform(post("/api/v1/groups/{groupId}/votings/{votingId}/bans", groupId, votingId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"memberId": "%s", "reason": "Duplicado"}
                                """.formatted(banMemberId)))
                .andExpect(status().isConflict());

        mvc.perform(delete("/api/v1/groups/{groupId}/votings/{votingId}/bans/{memberId}",
                        groupId, votingId, banMemberId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }
}
