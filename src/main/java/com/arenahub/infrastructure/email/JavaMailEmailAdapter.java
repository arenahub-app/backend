package com.arenahub.infrastructure.email;

import com.arenahub.application.auth.port.out.EmailSenderPort;
import com.arenahub.infrastructure.config.MailProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
public class JavaMailEmailAdapter implements EmailSenderPort {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));

    private final RestClient restClient;
    private final TemplateEngine templateEngine;
    private final MailProperties mailProperties;

    public JavaMailEmailAdapter(MailProperties mailProperties,
                                TemplateEngine templateEngine,
                                RestClient.Builder restClientBuilder) {
        this.mailProperties = mailProperties;
        this.templateEngine = templateEngine;
        this.restClient = restClientBuilder
                .baseUrl("https://api.resend.com")
                .defaultHeader("Authorization", "Bearer " + mailProperties.apiKey())
                .build();
    }

    @Override
    public void sendEmailVerification(String toEmail, String verificationUrl) {
        Context ctx = new Context(Locale.forLanguageTag("pt-BR"));
        ctx.setVariable("verificationUrl", verificationUrl);
        send(toEmail, "Confirme seu email — ArenaHub",
                templateEngine.process("email/email-verification", ctx));
    }

    @Override
    public void sendPasswordReset(String toEmail, String resetUrl) {
        Context ctx = new Context(Locale.forLanguageTag("pt-BR"));
        ctx.setVariable("resetUrl", resetUrl);
        send(toEmail, "Redefinição de senha — ArenaHub",
                templateEngine.process("email/password-reset", ctx));
    }

    @Override
    public void sendPasswordChanged(String toEmail, Instant changedAt) {
        Context ctx = new Context(Locale.forLanguageTag("pt-BR"));
        ctx.setVariable("changedAt", FORMATTER.format(changedAt));
        send(toEmail, "Sua senha foi alterada — ArenaHub",
                templateEngine.process("email/password-changed", ctx));
    }

    private void send(String to, String subject, String htmlBody) {
        restClient.post()
                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "from", mailProperties.from(),
                        "to", List.of(to),
                        "subject", subject,
                        "html", htmlBody
                ))
                .retrieve()
                .toBodilessEntity();
    }
}
