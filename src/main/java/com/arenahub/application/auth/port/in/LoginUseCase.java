package com.arenahub.application.auth.port.in;

import com.arenahub.application.auth.dto.AuthTokens;

public interface LoginUseCase {

    record Command(String email, String password) {}

    AuthTokens execute(Command command);
}
