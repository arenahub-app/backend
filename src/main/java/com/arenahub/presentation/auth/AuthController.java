package com.arenahub.presentation.auth;

import com.arenahub.application.auth.dto.AuthTokens;
import com.arenahub.application.auth.port.in.*;
import com.arenahub.infrastructure.config.JwtProperties;
import com.arenahub.presentation.auth.dto.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final RegisterUserUseCase registerUserUseCase;
    private final LoginUseCase loginUseCase;
    private final RefreshSessionUseCase refreshSessionUseCase;
    private final LogoutUseCase logoutUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final JwtProperties jwtProperties;

    public AuthController(RegisterUserUseCase registerUserUseCase,
                          LoginUseCase loginUseCase,
                          RefreshSessionUseCase refreshSessionUseCase,
                          LogoutUseCase logoutUseCase,
                          VerifyEmailUseCase verifyEmailUseCase,
                          JwtProperties jwtProperties) {
        this.registerUserUseCase = registerUserUseCase;
        this.loginUseCase = loginUseCase;
        this.refreshSessionUseCase = refreshSessionUseCase;
        this.logoutUseCase = logoutUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req,
                                                  HttpServletResponse response) {
        AuthTokens tokens = registerUserUseCase.execute(new RegisterUserUseCase.Command(
                req.name(), req.email(), req.password(), req.phone(), req.birthDate()));
        setRefreshTokenCookie(response, tokens.refreshToken());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(tokens.accessToken(), tokens.accessTokenExpiresInSeconds()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req,
                                               HttpServletResponse response) {
        AuthTokens tokens = loginUseCase.execute(
                new LoginUseCase.Command(req.email(), req.password()));
        setRefreshTokenCookie(response, tokens.refreshToken());
        return ResponseEntity.ok(
                new AuthResponse(tokens.accessToken(), tokens.accessTokenExpiresInSeconds()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request,
                                                 HttpServletResponse response) {
        String rawToken = extractRefreshTokenCookie(request);
        if (rawToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AuthTokens tokens = refreshSessionUseCase.execute(rawToken);
        setRefreshTokenCookie(response, tokens.refreshToken());
        return ResponseEntity.ok(
                new AuthResponse(tokens.accessToken(), tokens.accessTokenExpiresInSeconds()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = extractRefreshTokenCookie(request);
        logoutUseCase.execute(new LogoutUseCase.Command(rawToken));
        clearRefreshTokenCookie(response);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam String token) {
        verifyEmailUseCase.execute(new VerifyEmailUseCase.Command(token));
        return ResponseEntity.noContent().build();
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String rawToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", rawToken)
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth/refresh")
                .maxAge(Duration.ofDays(jwtProperties.refreshTokenExpirationDays()))
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .path("/api/v1/auth/refresh")
                .maxAge(Duration.ZERO)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private String extractRefreshTokenCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> "refresh_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
