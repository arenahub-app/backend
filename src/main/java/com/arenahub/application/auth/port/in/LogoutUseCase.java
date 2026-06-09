package com.arenahub.application.auth.port.in;

public interface LogoutUseCase {

    record Command(String rawRefreshToken) {}

    void execute(Command command);
}
