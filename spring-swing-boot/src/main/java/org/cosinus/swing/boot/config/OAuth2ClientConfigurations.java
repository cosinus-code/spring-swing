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

package org.cosinus.swing.boot.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cosinus.swing.resource.FilesystemResourceResolver;
import org.cosinus.swing.security.LocalStorageOAuth2AuthorizedClientService;
import org.cosinus.swing.security.ReceiverOAuth2AuthorizedClientProvider;
import org.cosinus.swing.security.properties.SwingOAuth2ClientProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.ConditionalOnOAuth2ClientRegistrationProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesMapper;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.*;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.security.receiver.LocalOAuth2AuthenticationReceiver.buildUrl;
import static org.cosinus.swing.security.receiver.OAuth2AuthenticationReceiver.DEFAULT_PORT;
import static org.cosinus.swing.security.receiver.OAuth2AuthenticationReceiver.LOCALHOST;
import static org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI;

public class OAuth2ClientConfigurations {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnOAuth2ClientRegistrationProperties
    @ConditionalOnBooleanProperty("spring.security.oauth2.client.receiver.enabled")
    @EnableConfigurationProperties(SwingOAuth2ClientProperties.class)
    @ConditionalOnMissingBean(ClientRegistrationRepository.class)
    static class SwingClientRegistrationRepositoryConfiguration {

        @Bean
        InMemoryClientRegistrationRepository clientRegistrationRepository(SwingOAuth2ClientProperties properties) {
            properties.getRegistration()
                .values()
                .stream()
                .filter(registration -> isNull(registration.getRedirectUri()))
                .forEach(registration -> registration.setRedirectUri(buildUrl(
                    ofNullable(properties.getReceiver())
                        .map(SwingOAuth2ClientProperties.OAuth2ClientReceiver::getHost)
                        .orElse(LOCALHOST),
                    ofNullable(properties.getReceiver())
                        .map(SwingOAuth2ClientProperties.OAuth2ClientReceiver::getPort)
                        .orElse(DEFAULT_PORT))));
            List<ClientRegistration> registrations = new ArrayList<>(
                new OAuth2ClientPropertiesMapper(properties).asClientRegistrations().values());
            return new InMemoryClientRegistrationRepository(registrations);
        }

        @Bean
        public OAuth2AuthorizationRequestResolver defaultOAuth2AuthorizationRequestResolver(
            final ClientRegistrationRepository clientRegistrationRepository) {

            return new DefaultOAuth2AuthorizationRequestResolver(
                clientRegistrationRepository, DEFAULT_AUTHORIZATION_REQUEST_BASE_URI);
        }

        @Bean
        public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> oAuth2AccessTokenResponseClient() {
            return new RestClientAuthorizationCodeTokenResponseClient();
        }

        @Bean
        public OAuth2AuthorizedClientManager authorizedClientManager(
            final OAuth2AuthorizedClientService oAuth2AuthorizedClientService,
            final ClientRegistrationRepository clientRegistrationRepository,
            final ReceiverOAuth2AuthorizedClientProvider receiverOAuth2AuthorizedClientProvider) {

            AuthorizedClientServiceOAuth2AuthorizedClientManager authorizedClientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(
                    clientRegistrationRepository,
                    oAuth2AuthorizedClientService);
            authorizedClientManager.setAuthorizedClientProvider(new DelegatingOAuth2AuthorizedClientProvider(
                new RefreshTokenOAuth2AuthorizedClientProvider(),
                receiverOAuth2AuthorizedClientProvider
            ));
            return authorizedClientManager;
        }

        @Bean
        public ReceiverOAuth2AuthorizedClientProvider receiverOAuth2AuthorizedClientProvider(
            final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient,
            @Value("${spring.security.oauth2.client.receiver.host:" + LOCALHOST + "}") final String receiverHost,
            @Value("${spring.security.oauth2.client.receiver.port:" + DEFAULT_PORT + "}") final int receiverPort) {
            return new ReceiverOAuth2AuthorizedClientProvider(accessTokenResponseClient, receiverHost, receiverPort);
        }

    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnBean(ClientRegistrationRepository.class)
    @ConditionalOnProperty("spring.security.oauth2.client.storing.location")
    static class OAuth2AuthorizedClientServiceConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public LocalStorageOAuth2AuthorizedClientService localStorageOAuth2AuthorizedClientService(
            final ObjectMapper objectMapper,
            final FilesystemResourceResolver filesystemResourceResolver,
            final ClientRegistrationRepository clientRegistrationRepository,
            @Value("${spring.security.oauth2.client.storing.location}") final String storingLocation) {

            return new LocalStorageOAuth2AuthorizedClientService(
                objectMapper, filesystemResourceResolver, clientRegistrationRepository, storingLocation);
        }

    }
}
