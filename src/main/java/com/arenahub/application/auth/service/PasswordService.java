package com.arenahub.application.auth.service;

import com.arenahub.application.auth.port.in.ForgotPasswordUseCase;
import com.arenahub.application.auth.port.in.ResetPasswordUseCase;
import com.arenahub.application.auth.port.out.EmailSenderPort;
import com.arenahub.application.exception.InvalidPurposeTokenException;
import com.arenahub.domain.user.*;
import com.arenahub.domain.user.vo.Email;
import com.arenahub.infrastructure.config.FrontendProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@Transactional
public class PasswordService implements ForgotPasswordUseCase, ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSenderPort emailSender;
    private final FrontendProperties frontendProperties;

    public PasswordService(UserRepository userRepository,
                           PasswordResetTokenRepository tokenRepository,
                           RefreshTokenRepository refreshTokenRepository,
                           PasswordEncoder passwordEncoder,
                           EmailSenderPort emailSender,
                           FrontendProperties frontendProperties) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.frontendProperties = frontendProperties;
    }

    @Override
    public void execute(ForgotPasswordUseCase.Command command) {
        userRepository.findByEmail(new Email(command.email())).ifPresent(user -> {
            String rawToken = UUID.randomUUID().toString();
            String tokenHash = AuthService.hashToken(rawToken);
            PasswordResetToken prt = PasswordResetToken.issue(user.getId(), tokenHash, Duration.ofHours(1));
            tokenRepository.save(prt);

            String resetUrl = frontendProperties.baseUrl() + "/reset-password?token=" + rawToken;
            String userEmail = user.getEmail().value();

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        emailSender.sendPasswordReset(userEmail, resetUrl);
                    } catch (Exception ex) {
                        log.error("Falha ao enviar email de reset de senha para {}", userEmail, ex);
                    }
                }
            });
        });
    }

    @Override
    public void execute(ResetPasswordUseCase.Command command) {
        String tokenHash = AuthService.hashToken(command.token());
        PasswordResetToken prt = tokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidPurposeTokenException("Token de redefinição inválido"));

        if (!prt.isValid()) {
            throw new InvalidPurposeTokenException("Token de redefinição expirado ou já utilizado");
        }

        User user = userRepository.findById(prt.getUserId())
                .orElseThrow(() -> new InvalidPurposeTokenException("Usuário não encontrado"));

        user.setPasswordHash(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);

        prt.markAsUsed();
        tokenRepository.save(prt);

        refreshTokenRepository.revokeAllByUserId(user.getId());

        String userEmail = user.getEmail().value();
        Instant changedAt = Instant.now();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    emailSender.sendPasswordChanged(userEmail, changedAt);
                } catch (Exception ex) {
                    log.error("Falha ao enviar email de senha alterada para {}", userEmail, ex);
                }
            }
        });
    }
}
