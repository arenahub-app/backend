package com.arenahub.presentation.auth;

import com.arenahub.application.auth.port.out.EmailSenderPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class AuthControllerIntegrationTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    EmailSenderPort emailSenderPort;

    private static final String REGISTER_URL = "/api/v1/auth/register";
    private static final String LOGIN_URL = "/api/v1/auth/login";
    private static final String LOGOUT_URL = "/api/v1/auth/logout";

    @Test
    void register_returns201_andAccessToken() throws Exception {
        mvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Integration Test",
                                  "email": "integration@test.com",
                                  "password": "Senha@123",
                                  "phone": "11999999999",
                                  "birthDate": "1990-01-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", not(emptyString())))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().httpOnly("refresh_token", true));
    }

    @Test
    void register_returns409_forDuplicateEmail() throws Exception {
        String body = """
                {
                  "name": "Dup User",
                  "email": "dup@test.com",
                  "password": "Senha@123",
                  "phone": "11888888888",
                  "birthDate": "1990-01-01"
                }
                """;

        mvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        mvc.perform(post(REGISTER_URL).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void register_returns400_forWeakPassword() throws Exception {
        mvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Test User",
                                  "email": "weak@test.com",
                                  "password": "senha123",
                                  "phone": "11999999999",
                                  "birthDate": "1990-01-01"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_returns403_whenEmailNotVerified() throws Exception {
        mvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Unverified User",
                                  "email": "unverified@test.com",
                                  "password": "Senha@123",
                                  "phone": "11777777777",
                                  "birthDate": "1990-01-01"
                                }
                                """))
                .andExpect(status().isCreated());

        mvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "unverified@test.com", "password": "Senha@123" }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void login_returns401_forWrongPassword() throws Exception {
        mvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "nobody@test.com", "password": "WrongPass@1" }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void logout_returns204() throws Exception {
        mvc.perform(post(LOGOUT_URL))
                .andExpect(status().isNoContent());
    }

    @Test
    void forgotPassword_returns200_forAnyEmail() throws Exception {
        mvc.perform(post("/api/v1/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "qualquer@example.com" }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void forgotPassword_returns400_forInvalidEmailFormat() throws Exception {
        mvc.perform(post("/api/v1/auth/password/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "nao-e-um-email" }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPassword_returns400_forUnknownToken() throws Exception {
        mvc.perform(post("/api/v1/auth/password/reset")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "token": "00000000-0000-0000-0000-000000000000",
                                  "newPassword": "NovaSenha@123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }
}
