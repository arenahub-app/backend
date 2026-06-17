package com.arenahub.application.auth.service;

import com.arenahub.application.auth.dto.AuthTokens;
import com.arenahub.application.auth.port.in.*;
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
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class AuthService implements RegisterUserUseCase, LoginUseCase,
        RefreshSessionUseCase, LogoutUseCase, VerifyEmailUseCase {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final EmailSenderPort emailSender;
    private final FrontendProperties frontendProperties;
    private final AuthProperties authProperties;

    public AuthService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtService jwtService,
                       JwtProperties jwtProperties,
                       PasswordEncoder passwordEncoder,
                       EmailSenderPort emailSender,
                       FrontendProperties frontendProperties,
                       AuthProperties authProperties) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.frontendProperties = frontendProperties;
        this.authProperties = authProperties;
    }

    @Override
    public AuthTokens execute(RegisterUserUseCase.Command command) {
        Email email = new Email(command.email());
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email.value());
        }

        String passwordHash = passwordEncoder.encode(command.password());
        User user = User.registerLocal(
                new Name(command.name()),
                email,
                passwordHash,
                new Phone(command.phone()),
                command.birthDate()
        );
        User saved = userRepository.save(user);

        if (authProperties.emailVerificationEnabled()) {
            String verificationToken = jwtService.generatePurposeToken(
                    saved.getId(), "email-verification", Duration.ofHours(24));
            String verificationUrl = frontendProperties.baseUrl()
                    + "/verify-email?token=" + verificationToken;
            String userEmail = saved.getEmail().value();
            runAfterCommit(() -> emailSender.sendEmailVerification(userEmail, verificationUrl));
        }

        return issueTokens(saved);
    }

    @Override
    public AuthTokens execute(LoginUseCase.Command command) {
        Email email = new Email(command.email());
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (!user.isActive()) throw new AccountInactiveException();
        if (authProperties.emailVerificationEnabled() && !user.isEmailVerified()) {
            throw new EmailNotVerifiedException();
        }
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        return issueTokens(user);
    }

    @Override
    public AuthTokens execute(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(InvalidRefreshTokenException::new);

        if (refreshToken.getStatus() == RefreshTokenStatus.REVOKED) {
            refreshTokenRepository.revokeAllByUserId(refreshToken.getUserId());
            throw new RefreshTokenReuseDetectedException();
        }

        if (!refreshToken.isValid()) {
            throw new InvalidRefreshTokenException();
        }

        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(InvalidRefreshTokenException::new);

        return issueTokens(user);
    }

    @Override
    public void execute(LogoutUseCase.Command command) {
        String rawRefreshToken = command.rawRefreshToken();
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) return;
        String tokenHash = hashToken(rawRefreshToken);
        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(rt -> {
            rt.revoke();
            refreshTokenRepository.save(rt);
        });
    }

    @Override
    public void execute(VerifyEmailUseCase.Command command) {
        Claims claims = jwtService.parseClaims(command.token())
                .filter(c -> "email-verification".equals(c.get("purpose")))
                .orElseThrow(() -> new InvalidPurposeTokenException("Token de verificação inválido"));

        UUID userId = UUID.fromString(claims.getSubject());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidPurposeTokenException("Usuário não encontrado"));

        user.verifyEmail();
        userRepository.save(user);
    }

    AuthTokens issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = UUID.randomUUID().toString();
        String tokenHash = hashToken(rawRefreshToken);
        Duration refreshTtl = Duration.ofDays(jwtProperties.refreshTokenExpirationDays());
        RefreshToken rt = RefreshToken.issue(user.getId(), tokenHash, refreshTtl);
        refreshTokenRepository.save(rt);
        return new AuthTokens(accessToken, rawRefreshToken, jwtService.accessTokenExpirationSeconds());
    }

    private void runAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        task.run();
                    } catch (Exception ex) {
                        log.error("Falha ao enviar email", ex);
                    }
                }
            });
        } else {
            task.run();
        }
    }

    static String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
