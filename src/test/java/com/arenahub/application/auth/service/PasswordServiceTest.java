package com.arenahub.application.auth.service;

import com.arenahub.application.auth.port.in.ForgotPasswordUseCase;
import com.arenahub.application.auth.port.in.ResetPasswordUseCase;
import com.arenahub.application.auth.port.out.EmailSenderPort;
import com.arenahub.application.exception.InvalidPurposeTokenException;
import com.arenahub.domain.user.*;
import com.arenahub.domain.user.vo.Email;
import com.arenahub.domain.user.vo.Name;
import com.arenahub.domain.user.vo.Phone;
import com.arenahub.infrastructure.config.FrontendProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordResetTokenRepository tokenRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock EmailSenderPort emailSender;

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService(
                userRepository, tokenRepository, refreshTokenRepository,
                new BCryptPasswordEncoder(), emailSender,
                new FrontendProperties("http://localhost:5173"));
    }

    @Test
    void forgotPassword_savesTokenAndSendsEmail_whenUserExists() {
        User user = buildUser();
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        passwordService.execute(new ForgotPasswordUseCase.Command("joao@example.com"));

        verify(tokenRepository).save(any(PasswordResetToken.class));
        verify(emailSender).sendPasswordReset(eq("joao@example.com"), contains("reset-password?token="));
    }

    @Test
    void forgotPassword_isNoop_whenUserNotFound() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        passwordService.execute(new ForgotPasswordUseCase.Command("nobody@example.com"));

        verifyNoInteractions(tokenRepository, emailSender);
    }

    @Test
    void resetPassword_updatesPasswordRevokesTokensAndSendsEmail_whenValid() {
        User user = buildUser();
        String rawToken = "valid-reset-token-" + UUID.randomUUID();
        String tokenHash = AuthService.hashToken(rawToken);
        PasswordResetToken prt = PasswordResetToken.issue(user.getId(), tokenHash, Duration.ofHours(1));

        when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(prt));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(tokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        passwordService.execute(new ResetPasswordUseCase.Command(rawToken, "NovaSenha@123"));

        assertThat(prt.isValid()).isFalse();
        verify(refreshTokenRepository).revokeAllByUserId(user.getId());
        verify(emailSender).sendPasswordChanged(eq("joao@example.com"), any());
    }

    @Test
    void resetPassword_throws_whenTokenNotFound() {
        when(tokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                passwordService.execute(new ResetPasswordUseCase.Command("invalid-token", "NovaSenha@123")))
                .isInstanceOf(InvalidPurposeTokenException.class);
    }

    @Test
    void resetPassword_throws_whenTokenAlreadyUsed() {
        String rawToken = "used-token-" + UUID.randomUUID();
        String tokenHash = AuthService.hashToken(rawToken);
        PasswordResetToken usedToken = PasswordResetToken.issue(UUID.randomUUID(), tokenHash, Duration.ofHours(1));
        usedToken.markAsUsed();

        when(tokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(usedToken));

        assertThatThrownBy(() ->
                passwordService.execute(new ResetPasswordUseCase.Command(rawToken, "NovaSenha@123")))
                .isInstanceOf(InvalidPurposeTokenException.class)
                .hasMessageContaining("expirado ou já utilizado");
    }

    private User buildUser() {
        return User.registerLocal(
                new Name("João"), new Email("joao@example.com"),
                "hash", new Phone("11999999999"), LocalDate.of(1990, 1, 1));
    }
}
