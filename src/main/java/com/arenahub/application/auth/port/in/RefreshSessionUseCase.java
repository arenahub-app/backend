package com.arenahub.application.auth.port.in;

import com.arenahub.application.auth.dto.AuthTokens;

public interface RefreshSessionUseCase {

    AuthTokens execute(String rawRefreshToken);
}
