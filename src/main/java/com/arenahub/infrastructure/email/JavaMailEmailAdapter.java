package com.arenahub.infrastructure.email;

import com.arenahub.application.auth.port.out.EmailSenderPort;
import com.arenahub.infrastructure.config.MailProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class JavaMailEmailAdapter implements EmailSenderPort {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final MailProperties mailProperties;

    public JavaMailEmailAdapter(JavaMailSender mailSender,
                                 TemplateEngine templateEngine,
                                 MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.mailProperties = mailProperties;
    }

    @Override
    public void sendEmailVerification(String toEmail, String verificationUrl) {
        Context ctx = new Context(Locale.forLanguageTag("pt-BR"));
        ctx.setVariable("verificationUrl", verificationUrl);
        String html = templateEngine.process("email/email-verification", ctx);
        send(toEmail, "Confirme seu email — ArenaHub", html);
    }

    @Override
    public void sendPasswordReset(String toEmail, String resetUrl) {
        Context ctx = new Context(Locale.forLanguageTag("pt-BR"));
        ctx.setVariable("resetUrl", resetUrl);
        String html = templateEngine.process("email/password-reset", ctx);
        send(toEmail, "Redefinição de senha — ArenaHub", html);
    }

    @Override
    public void sendPasswordChanged(String toEmail, Instant changedAt) {
        Context ctx = new Context(Locale.forLanguageTag("pt-BR"));
        ctx.setVariable("changedAt", FORMATTER.format(changedAt));
        String html = templateEngine.process("email/password-changed", ctx);
        send(toEmail, "Sua senha foi alterada — ArenaHub", html);
    }

    private void send(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(mailProperties.from());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Falha ao enviar email para " + to, e);
        }
    }
}
