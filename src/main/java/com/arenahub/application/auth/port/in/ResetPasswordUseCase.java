package com.arenahub.application.auth.port.in;

public interface ResetPasswordUseCase {

    record Command(String token, String newPassword) {}

    void execute(Command command);
}
