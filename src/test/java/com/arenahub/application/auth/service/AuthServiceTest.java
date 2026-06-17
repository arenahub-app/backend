package com.arenahub.application.auth.service;

import com.arenahub.application.auth.dto.AuthTokens;
import com.arenahub.application.auth.port.in.LoginUseCase;
import com.arenahub.application.auth.port.in.RegisterUserUseCase;
import com.arenahub.application.auth.port.out.EmailSenderPort;
import com.arenahub.application.exception.*;
import com.arenahub.domain.user.*;
import com.arenahub.domain.user.vo.Email;
import com.arenahub.domain.user.vo.Name;
import com.arenahub.domain.user.vo.Phone;
import com.arenahub.infrastructure.config.AuthProperties;
import com.arenahub.infrastructure.config.FrontendProperties;
import com.arenahub.infrastructure.config.JwtProperties;
import com.arenahub.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock EmailSenderPort emailSender;

    private AuthService authService;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        JwtProperties jwtProps = new JwtProperties(
                "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm", 15, 7);
        jwtService = new JwtService(jwtProps);
        FrontendProperties frontendProps = new FrontendProperties("http://localhost:5173");

        authService = new AuthService(userRepository, refreshTokenRepository, jwtService,
                jwtProps, passwordEncoder, emailSender, frontendProps, new AuthProperties(false));
    }

    @Test
    void register_savesUserAndSendsVerificationEmail_whenToggleOn() {
        AuthService svc = serviceWithVerification(true);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterUserUseCase.Command cmd = new RegisterUserUseCase.Command(
                "João Silva", "joao@example.com", "Senha@123", "11999999999",
                LocalDate.of(1990, 1, 1));

        AuthTokens tokens = svc.execute(cmd);

        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(tokens.refreshToken()).isNotBlank();
        verify(emailSender).sendEmailVerification(eq("joao@example.com"), anyString());
    }

    @Test
    void register_doesNotSendVerificationEmail_whenToggleOff() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        RegisterUserUseCase.Command cmd = new RegisterUserUseCase.Command(
                "João Silva", "joao@example.com", "Senha@123", "11999999999",
                LocalDate.of(1990, 1, 1));

        AuthTokens tokens = authService.execute(cmd);

        assertThat(tokens.accessToken()).isNotBlank();
        verifyNoInteractions(emailSender);
    }

    @Test
    void register_throwsEmailAlreadyRegistered_whenEmailExists() {
        when(userRepository.existsByEmail(any())).thenReturn(true);

        RegisterUserUseCase.Command cmd = new RegisterUserUseCase.Command(
                "João", "joao@example.com", "Senha@123", "11999999999", LocalDate.of(1990, 1, 1));

        assertThatThrownBy(() -> authService.execute(cmd))
                .isInstanceOf(EmailAlreadyRegisteredException.class);
    }

    @Test
    void login_returnsTokens_forValidCredentials() {
        String hash = passwordEncoder.encode("Senha@123");
        User user = User.registerLocal(
                new Name("João"), new Email("joao@example.com"), hash,
                new Phone("11999999999"), LocalDate.of(1990, 1, 1));
        user.verifyEmail();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthTokens tokens = authService.execute(new LoginUseCase.Command("joao@example.com", "Senha@123"));

        assertThat(tokens.accessToken()).isNotBlank();
    }

    @Test
    void login_throws_forWrongPassword() {
        String hash = passwordEncoder.encode("Senha@123");
        User user = User.registerLocal(
                new Name("João"), new Email("joao@example.com"), hash,
                new Phone("11999999999"), LocalDate.of(1990, 1, 1));
        user.verifyEmail();

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.execute(
                new LoginUseCase.Command("joao@example.com", "SenhaErrada@1")))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_throws_whenEmailNotVerified_andToggleOn() {
        AuthService svc = serviceWithVerification(true);
        String hash = passwordEncoder.encode("Senha@123");
        User user = User.registerLocal(
                new Name("João"), new Email("joao@example.com"), hash,
                new Phone("11999999999"), LocalDate.of(1990, 1, 1));

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> svc.execute(
                new LoginUseCase.Command("joao@example.com", "Senha@123")))
                .isInstanceOf(EmailNotVerifiedException.class);
    }

    @Test
    void login_succeeds_whenEmailNotVerified_andToggleOff() {
        String hash = passwordEncoder.encode("Senha@123");
        User user = User.registerLocal(
                new Name("João"), new Email("joao@example.com"), hash,
                new Phone("11999999999"), LocalDate.of(1990, 1, 1));

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthTokens tokens = authService.execute(
                new LoginUseCase.Command("joao@example.com", "Senha@123"));

        assertThat(tokens.accessToken()).isNotBlank();
    }

    @Test
    void refresh_revokesOldTokenAndIssuesNew() {
        String rawToken = "raw-refresh-token-uuid";
        String hash = AuthService.hashToken(rawToken);

        User user = buildVerifiedUser();
        RefreshToken rt = RefreshToken.issue(user.getId(), hash,
                java.time.Duration.ofDays(7));

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(rt));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AuthTokens tokens = authService.execute(rawToken);

        assertThat(tokens.accessToken()).isNotBlank();
        assertThat(rt.getStatus()).isEqualTo(RefreshTokenStatus.REVOKED);
    }

    @Test
    void refresh_throwsReuseDetected_forRevokedToken() {
        String rawToken = "reused-token";
        String hash = AuthService.hashToken(rawToken);

        User user = buildVerifiedUser();
        RefreshToken rt = RefreshToken.issue(user.getId(), hash, java.time.Duration.ofDays(7));
        rt.revoke();

        when(refreshTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(rt));

        assertThatThrownBy(() -> authService.execute(rawToken))
                .isInstanceOf(RefreshTokenReuseDetectedException.class);

        verify(refreshTokenRepository).revokeAllByUserId(user.getId());
    }

    private User buildVerifiedUser() {
        User user = User.registerLocal(
                new Name("João"), new Email("joao@example.com"), "hash",
                new Phone("11999999999"), LocalDate.of(1990, 1, 1));
        user.verifyEmail();
        return user;
    }

    private AuthService serviceWithVerification(boolean enabled) {
        JwtProperties jwtProps = new JwtProperties(
                "test-secret-key-must-be-at-least-256-bits-long-for-hs256-algorithm", 15, 7);
        FrontendProperties frontendProps = new FrontendProperties("http://localhost:5173");
        return new AuthService(userRepository, refreshTokenRepository,
                new JwtService(jwtProps), jwtProps, passwordEncoder,
                emailSender, frontendProps, new AuthProperties(enabled));
    }
}
