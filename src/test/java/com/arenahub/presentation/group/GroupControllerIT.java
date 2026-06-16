package com.arenahub.presentation.group;

import com.arenahub.application.auth.port.out.EmailSenderPort;
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
class GroupControllerIT {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    EmailSenderPort emailSenderPort;

    private String accessToken;

    @BeforeEach
    void setUpAuthenticatedUser() throws Exception {
        Mockito.reset(emailSenderPort);

        String email = "grp-" + UUID.randomUUID() + "@test.com";

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Group Tester",
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

        accessToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("accessToken").asText();
    }

    @Test
    void createGroup_returns201_withOwnerRole() throws Exception {
        mvc.perform(post("/api/v1/groups")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Arena FC",
                                  "sport": "FOOTBALL",
                                  "description": "Futebol semanal"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", not(emptyString())))
                .andExpect(jsonPath("$.name").value("Arena FC"))
                .andExpect(jsonPath("$.sport").value("FOOTBALL"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.memberCount").value(1))
                .andExpect(jsonPath("$.myRole").value("OWNER"));
    }

    @Test
    void createGroup_returns401_withoutToken() throws Exception {
        mvc.perform(post("/api/v1/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Arena FC", "sport": "FOOTBALL"}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createGroup_returns400_forInvalidName() throws Exception {
        mvc.perform(post("/api/v1/groups")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "AB", "sport": "FOOTBALL"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listGroups_returnsCreatedGroup() throws Exception {
        mvc.perform(post("/api/v1/groups")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Lista FC", "sport": "VOLLEYBALL"}
                                """))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/groups")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].name", not(emptyString())));
    }

    @Test
    void getGroup_returnsGroupDetails() throws Exception {
        MvcResult createResult = mvc.perform(post("/api/v1/groups")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Detail FC", "sport": "BASKETBALL"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String groupId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(get("/api/v1/groups/{id}", groupId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(groupId))
                .andExpect(jsonPath("$.name").value("Detail FC"))
                .andExpect(jsonPath("$.myRole").value("OWNER"));
    }

    @Test
    void updateGroup_returns200_forOwner() throws Exception {
        MvcResult createResult = mvc.perform(post("/api/v1/groups")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Update FC", "sport": "FOOTBALL"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String groupId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(patch("/api/v1/groups/{id}", groupId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Updated FC", "pixKey": "pix@arena.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated FC"))
                .andExpect(jsonPath("$.pixKey").value("pix@arena.com"));
    }

    @Test
    void listMembers_returnsOwnerAsMember() throws Exception {
        MvcResult createResult = mvc.perform(post("/api/v1/groups")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Members FC", "sport": "FOOTBALL"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String groupId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(get("/api/v1/groups/{id}/members", groupId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].role").value("OWNER"));
    }

    @Test
    void generateInvite_returns201_withToken() throws Exception {
        MvcResult createResult = mvc.perform(post("/api/v1/groups")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Invite FC", "sport": "FOOTBALL"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String groupId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(post("/api/v1/groups/{id}/invites", groupId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", not(emptyString())))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.maxUsages").value(50));
    }

    @Test
    void listInvites_returnsActiveInvites() throws Exception {
        MvcResult createResult = mvc.perform(post("/api/v1/groups")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "InvList FC", "sport": "FOOTBALL"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String groupId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(post("/api/v1/groups/{id}/invites", groupId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/v1/groups/{id}/invites", groupId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void invitePreview_returns200_withoutAuth() throws Exception {
        MvcResult createResult = mvc.perform(post("/api/v1/groups")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Preview FC", "sport": "FOOTBALL"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String groupId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        MvcResult inviteResult = mvc.perform(post("/api/v1/groups/{id}/invites", groupId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();

        String token = objectMapper.readTree(inviteResult.getResponse().getContentAsString())
                .get("token").asText();

        mvc.perform(get("/api/v1/invites/{token}", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupName").value("Preview FC"))
                .andExpect(jsonPath("$.memberCount").value(1));
    }

    @Test
    void deactivateInvite_returns204() throws Exception {
        MvcResult createResult = mvc.perform(post("/api/v1/groups")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "DeactInv FC", "sport": "FOOTBALL"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String groupId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        MvcResult inviteResult = mvc.perform(post("/api/v1/groups/{id}/invites", groupId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isCreated())
                .andReturn();

        String inviteId = objectMapper.readTree(inviteResult.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(delete("/api/v1/groups/{id}/invites/{inviteId}", groupId, inviteId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void deactivateGroup_returns204_forOwner() throws Exception {
        MvcResult createResult = mvc.perform(post("/api/v1/groups")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Deact FC", "sport": "FOOTBALL"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String groupId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        mvc.perform(delete("/api/v1/groups/{id}", groupId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void getGroup_returns403_forNonMember() throws Exception {
        MvcResult createResult = mvc.perform(post("/api/v1/groups")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name": "Private FC", "sport": "FOOTBALL"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String groupId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asText();

        Mockito.reset(emailSenderPort);
        String email2 = "grp2-" + UUID.randomUUID() + "@test.com";
        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Another User",
                                  "email": "%s",
                                  "password": "Senha@123",
                                  "phone": "11900000002",
                                  "birthDate": "1991-01-01"
                                }
                                """.formatted(email2)))
                .andExpect(status().isCreated());

        ArgumentCaptor<String> cap2 = ArgumentCaptor.forClass(String.class);
        verify(emailSenderPort).sendEmailVerification(eq(email2), cap2.capture());
        mvc.perform(get("/api/v1/auth/verify-email")
                        .param("token", cap2.getValue().replaceAll(".*token=", "")))
                .andExpect(status().isNoContent());

        MvcResult loginResult2 = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email": "%s", "password": "Senha@123"}
                                """.formatted(email2)))
                .andExpect(status().isOk())
                .andReturn();

        String token2 = objectMapper.readTree(loginResult2.getResponse().getContentAsString())
                .get("accessToken").asText();

        mvc.perform(get("/api/v1/groups/{id}", groupId)
                        .header("Authorization", "Bearer " + token2))
                .andExpect(status().isForbidden());
    }
}
