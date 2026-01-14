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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cosinus.swing.resource.FilesystemResourceResolver;
import org.cosinus.swing.security.properties.SwingOAuth2ClientProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;

public class LocalStorageOAuth2AuthorizedClientService
    implements OAuth2AuthorizedClientService {

    public static final String REFRESH_TOKEN_EXPIRES_IN = "refresh_token_expires_in";

    private final ClientRegistrationRepository clientRegistrationRepository;

    private final AuthorizedClients authorizedClients;

    public LocalStorageOAuth2AuthorizedClientService(final ObjectMapper objectMapper,
                                                     final FilesystemResourceResolver filesystemResourceResolver,
                                                     final ClientRegistrationRepository clientRegistrationRepository,
                                                     final SwingOAuth2ClientProperties properties) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        authorizedClients = new AuthorizedClients(objectMapper, filesystemResourceResolver, properties);
    }

    @Override
    public DetailedOAuth2AuthorizedClient loadAuthorizedClient(String clientRegistrationId, String principalName) {
        return ofNullable(authorizedClients.getAuthorizedClient(clientRegistrationId, principalName))
            .flatMap(authorizedClient -> ofNullable(authorizedClient.getRegistrationId())
                .map(clientRegistrationRepository::findByRegistrationId)
                .map(authorizedClient::toOAuth2AuthorizedClient))
            .orElse(null);
    }

    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient oAuth2AuthorizedClient, Authentication principal) {
        AuthorizedClient authorizedClient = new AuthorizedClient(oAuth2AuthorizedClient);

        Map<String, Object> details = ofNullable(authorizedClient.getDetails())
            .orElseGet(HashMap::new);

        String registrationId = oAuth2AuthorizedClient.getClientRegistration().getRegistrationId();
        ofNullable(authorizedClients.getAuthorizedClient(registrationId, principal.getName()))
            .map(AuthorizedClient::getDetails)
            .stream()
            .flatMap(existingDetails -> existingDetails.entrySet().stream())
            .filter(entry -> ofNullable(details.get(entry.getKey()))
                .map(newValue -> !newValue.equals(entry.getValue()))
                .orElse(true))
            .forEach(entry -> details.put(entry.getKey(), entry.getValue()));
        ofNullable(details.get(REFRESH_TOKEN_EXPIRES_IN))
            .map(Object::toString)
            .map(Long::parseLong)
            .ifPresent(authorizedClient::setRefreshTokenExpiresIn);
        authorizedClient.setDetails(details);

        authorizedClients.setAuthorizedClient(registrationId, principal.getName(), authorizedClient);
        save(registrationId, principal.getName());
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
        authorizedClients.removeAuthorizedClient(clientRegistrationId, principalName);
    }

    public void save(String registrationId, String principalName) {
        authorizedClients.saveAuthorizedClient(registrationId, principalName);
    }

    public Map<String, AuthorizedClient> getAuthorizedClientsMap(String registrationId) {
        return authorizedClients.getAuthorizedClientsMap(registrationId);
    }
}

