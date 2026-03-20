package com.intermediate.Blog.Application.Configurations;

import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/**
 * Adds prompt=select_account to Google OAuth2 authorization requests
 * so the account picker is shown every time (e.g. after logout).
 */
public final class OAuth2AuthorizationRequestCustomizer {

    private OAuth2AuthorizationRequestCustomizer() {}

    public static OAuth2AuthorizationRequestResolver createResolver(
            ClientRegistrationRepository clientRegistrationRepository) {
        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        clientRegistrationRepository,
                        "/oauth2/authorization");

        resolver.setAuthorizationRequestCustomizer(
                OAuth2AuthorizationRequestCustomizer::addPromptSelectAccount);

        return resolver;
    }

    private static void addPromptSelectAccount(OAuth2AuthorizationRequest.Builder builder) {
        builder.additionalParameters(params -> params.put("prompt", "select_account"));
    }
}
