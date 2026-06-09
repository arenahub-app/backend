package com.arenahub.application.auth.port.in;

import com.arenahub.application.auth.dto.AuthTokens;

import java.time.LocalDate;

public interface RegisterUserUseCase {

    record Command(
            String name,
            String email,
            String password,
            String phone,
            LocalDate birthDate
    ) {}

    AuthTokens execute(Command command);
}
