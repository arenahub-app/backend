package com.arenahub.application.auth.service;

import com.arenahub.application.auth.dto.AuthTokens;
import com.arenahub.application.auth.port.in.OAuthLoginUseCase;
import com.arenahub.domain.user.User;
import com.arenahub.domain.user.UserRepository;
import com.arenahub.domain.user.vo.Email;
import com.arenahub.domain.user.vo.Name;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class OAuthService implements OAuthLoginUseCase {

    private final UserRepository userRepository;
    private final AuthService authService;

    public OAuthService(UserRepository userRepository, AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    @Override
    public AuthTokens execute(GoogleUserInfo info) {
        Email email = new Email(info.email());

        User user = userRepository.findByGoogleId(info.googleId())
                .or(() -> userRepository.findByEmail(email))
                .map(existing -> linkGoogleIfNeeded(existing, info.googleId()))
                .orElseGet(() -> createFromGoogle(info));

        return authService.issueTokens(user);
    }

    private User linkGoogleIfNeeded(User user, String googleId) {
        if (user.getGoogleId() == null) {
            user.linkGoogleAccount(googleId);
            return userRepository.save(user);
        }
        return user;
    }

    private User createFromGoogle(GoogleUserInfo info) {
        User user = User.fromGoogle(info.googleId(), new Name(info.name()), new Email(info.email()));
        return userRepository.save(user);
    }
}
