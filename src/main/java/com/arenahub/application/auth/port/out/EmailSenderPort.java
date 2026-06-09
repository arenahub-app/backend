package com.arenahub.application.auth.port.out;

import java.time.Instant;

public interface EmailSenderPort {

    void sendEmailVerification(String toEmail, String verificationUrl);

    void sendPasswordReset(String toEmail, String resetUrl);

    void sendPasswordChanged(String toEmail, Instant changedAt);
}
