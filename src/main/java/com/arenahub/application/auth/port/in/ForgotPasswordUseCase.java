package com.arenahub.application.auth.port.in;

public interface ForgotPasswordUseCase {

    record Command(String email) {}

    void execute(Command command);
}
