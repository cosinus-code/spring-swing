/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.swing.security;

import org.cosinus.swing.security.receiver.OAuth2AuthenticationReceiver;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.event.OAuth2AuthorizedClientRefreshedEvent;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Clock;
import java.time.Duration;

import static java.util.Base64.getUrlEncoder;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.security.receiver.LocalOAuth2AuthenticationReceiver.callbackReceiver;
import static org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest.authorizationCode;
import static org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames.REGISTRATION_ID;

public class ReceiverOAuth2AuthorizedClientProvider
    implements OAuth2AuthorizedClientProvider, ApplicationEventPublisherAware {

    private static final StringKeyGenerator DEFAULT_STATE_GENERATOR = new Base64StringKeyGenerator(getUrlEncoder());

    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient;

    private ApplicationEventPublisher applicationEventPublisher;

    private final Duration clockSkew = Duration.ofSeconds(60);

    private final Clock clock = Clock.systemUTC();

    private final String receiverHost;

    private final int receiverPort;

    public ReceiverOAuth2AuthorizedClientProvider(
        final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient,
        final String receiverHost,
        final int receiverPort) {

        this.accessTokenResponseClient = accessTokenResponseClient;
        this.receiverHost = receiverHost;
        this.receiverPort = receiverPort;
    }

    @Override
    public OAuth2AuthorizedClient authorize(OAuth2AuthorizationContext context) {
        OAuth2AuthorizedClient initialAuthorizedClient = context.getAuthorizedClient();
        if (initialAuthorizedClient != null && !hasTokenExpired(initialAuthorizedClient.getAccessToken())) {
            return initialAuthorizedClient;
        }

        ClientRegistration clientRegistration = context.getClientRegistration();
        try (OAuth2AuthenticationReceiver receiver = callbackReceiver(receiverHost, receiverPort)) {
            receiver.start();

            OAuth2AuthorizationRequest authorizationRequest = createAuthorizationRequest(clientRegistration);
            OAuth2AuthorizationResponse authorizationResponse = receiver.execute(authorizationRequest);

            OAuth2AccessTokenResponse tokenResponse = exchangeAuthorizationForAccessToken(
                clientRegistration, authorizationRequest, authorizationResponse);

            OAuth2AuthorizedClient authorizedClient = new DetailedOAuth2AuthorizedClient(
                clientRegistration,
                context.getPrincipal().getName(),
                tokenResponse.getAccessToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getAdditionalParameters());

            if (this.applicationEventPublisher != null) {
                OAuth2AuthorizedClientRefreshedEvent authorizedClientRefreshedEvent =
                    new OAuth2AuthorizedClientRefreshedEvent(tokenResponse, authorizedClient);
                this.applicationEventPublisher.publishEvent(authorizedClientRefreshedEvent);
            }

            return authorizedClient;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    protected OAuth2AuthorizationRequest createAuthorizationRequest(final ClientRegistration clientRegistration) {
        return authorizationCode()
            .attributes(attrs ->
                attrs.put(REGISTRATION_ID, clientRegistration.getRegistrationId()))
            .clientId(clientRegistration.getClientId())
            .authorizationUri(clientRegistration.getProviderDetails().getAuthorizationUri())
            .redirectUri(clientRegistration.getRedirectUri())
            .scopes(clientRegistration.getScopes())
            .state(DEFAULT_STATE_GENERATOR.generateKey())
            .build();
    }

    protected OAuth2AccessTokenResponse exchangeAuthorizationForAccessToken(
        final ClientRegistration clientRegistration,
        final OAuth2AuthorizationRequest authorizationRequest,
        final OAuth2AuthorizationResponse authorizationResponse) {

        OAuth2AuthorizationExchange authorizationExchange = new OAuth2AuthorizationExchange(
            authorizationRequest, authorizationResponse);

        return accessTokenResponseClient.getTokenResponse(
            new OAuth2AuthorizationCodeGrantRequest(clientRegistration, authorizationExchange));
    }

    private boolean hasTokenExpired(OAuth2Token token) {
        return ofNullable(token.getExpiresAt())
            .map(expiresAt -> expiresAt.minus(this.clockSkew))
            .map(clock.instant()::isAfter)
            .orElse(false);
    }

    @Override
    public void setApplicationEventPublisher(final ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }
}