package com.arenahub.application.auth.service;

import com.arenahub.application.auth.dto.AuthTokens;
import com.arenahub.application.auth.port.in.OAuthLoginUseCase;
import com.arenahub.domain.user.User;
import com.arenahub.domain.user.UserRepository;
import com.arenahub.domain.user.vo.Email;
import com.arenahub.domain.user.vo.Name;
import com.arenahub.domain.user.vo.Phone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock AuthService authService;

    private OAuthService oAuthService;

    private static final AuthTokens MOCK_TOKENS = new AuthTokens("access-token", "refresh-token", 900L);

    @BeforeEach
    void setUp() {
        oAuthService = new OAuthService(userRepository, authService);
    }

    @Test
    void execute_createsNewUser_whenNeitherGoogleIdNorEmailExists() {
        var info = new OAuthLoginUseCase.GoogleUserInfo("gid-new", "novo@test.com", "Novo User", null);
        User newUser = User.fromGoogle("gid-new", new Name("Novo User"), new Email("novo@test.com"));

        when(userRepository.findByGoogleId("gid-new")).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(authService.issueTokens(any(User.class))).thenReturn(MOCK_TOKENS);

        AuthTokens result = oAuthService.execute(info);

        assertThat(result).isEqualTo(MOCK_TOKENS);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void execute_linksGoogleAccount_whenExistingUserFoundByEmail() {
        var info = new OAuthLoginUseCase.GoogleUserInfo("gid-link", "existente@test.com", "Existente", null);
        User existing = User.registerLocal(
                new Name("Existente"), new Email("existente@test.com"),
                "hash", new Phone("11999999999"), LocalDate.of(1990, 1, 1));

        when(userRepository.findByGoogleId("gid-link")).thenReturn(Optional.empty());
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(existing));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authService.issueTokens(any(User.class))).thenReturn(MOCK_TOKENS);

        oAuthService.execute(info);

        assertThat(existing.getGoogleId()).isEqualTo("gid-link");
        verify(userRepository).save(existing);
    }

    @Test
    void execute_returnsTokens_forExistingGoogleUser_withoutSaving() {
        var info = new OAuthLoginUseCase.GoogleUserInfo("gid-existing", "google@test.com", "Google User", null);
        User googleUser = User.fromGoogle("gid-existing", new Name("Google User"), new Email("google@test.com"));

        when(userRepository.findByGoogleId("gid-existing")).thenReturn(Optional.of(googleUser));
        when(authService.issueTokens(any(User.class))).thenReturn(MOCK_TOKENS);

        AuthTokens result = oAuthService.execute(info);

        assertThat(result).isEqualTo(MOCK_TOKENS);
        verify(userRepository, never()).save(any());
    }
}
