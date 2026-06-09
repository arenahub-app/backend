package com.arenahub.application.auth.port.in;

public interface VerifyEmailUseCase {

    record Command(String token) {}

    void execute(Command command);
}
