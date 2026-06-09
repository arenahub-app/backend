package com.arenahub.application.auth.port.in;

import com.arenahub.application.auth.dto.AuthTokens;

public interface OAuthLoginUseCase {

    record GoogleUserInfo(
            String googleId,
            String email,
            String name,
            String pictureUrl
    ) {}

    AuthTokens execute(GoogleUserInfo userInfo);
}
