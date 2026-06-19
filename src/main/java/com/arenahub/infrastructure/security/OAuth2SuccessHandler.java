package com.arenahub.infrastructure.security;

import com.arenahub.application.auth.dto.AuthTokens;
import com.arenahub.application.auth.port.in.OAuthLoginUseCase;
import com.arenahub.infrastructure.config.FrontendProperties;
import com.arenahub.infrastructure.config.JwtProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuthLoginUseCase oAuthLoginUseCase;
    private final JwtProperties jwtProperties;
    private final FrontendProperties frontendProperties;

    public OAuth2SuccessHandler(OAuthLoginUseCase oAuthLoginUseCase,
                                JwtProperties jwtProperties,
                                FrontendProperties frontendProperties) {
        this.oAuthLoginUseCase = oAuthLoginUseCase;
        this.jwtProperties = jwtProperties;
        this.frontendProperties = frontendProperties;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken oauth2 = (OAuth2AuthenticationToken) authentication;
        Map<String, Object> attrs = oauth2.getPrincipal().getAttributes();

        OAuthLoginUseCase.GoogleUserInfo userInfo = new OAuthLoginUseCase.GoogleUserInfo(
                (String) attrs.get("sub"),
                (String) attrs.get("email"),
                (String) attrs.get("name"),
                (String) attrs.get("picture")
        );

        AuthTokens tokens = oAuthLoginUseCase.execute(userInfo);
        setRefreshTokenCookie(response, tokens.refreshToken());

        String redirectUrl = frontendProperties.baseUrl()
                + "/auth/callback?token=" + tokens.accessToken();
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String rawToken) {
        ResponseCookie cookie = ResponseCookie.from("refresh_token", rawToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(Duration.ofDays(jwtProperties.refreshTokenExpirationDays()))
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
