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

import java.util.HashMap;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

public class AuthorizedClients extends HashMap<String, Map<String, AuthorizedClient>> {

    private final Map<String, AuthorizedClientProvider> authorizedClientProviders;

    public AuthorizedClients(final ObjectMapper objectMapper,
                             final FilesystemResourceResolver filesystemResourceResolver,
                             final SwingOAuth2ClientProperties properties) {
        authorizedClientProviders = properties.getReceiver()
            .entrySet()
            .stream()
            .collect(toMap(
                Entry::getKey,
                entry -> new AuthorizedClientProvider(
                    objectMapper,
                    filesystemResourceResolver,
                    entry.getValue().getStoringLocation(),
                    entry.getValue().getFileName()),
                (first, second) -> second));
    }

    public AuthorizedClient getAuthorizedClient(final String registrationId,
                                                final String principalName) {
        return getAuthorizedClientsMap(registrationId).get(principalName);
    }

    public AuthorizedClient setAuthorizedClient(final String registrationId,
                                                final String principalName,
                                                final AuthorizedClient authorizedClient) {
        return computeIfAbsent(registrationId, key -> new HashMap<>())
            .put(principalName, authorizedClient);
    }

    public void removeAuthorizedClient(final String registrationId,
                                       final String principalName) {
        remove(principalName);
        ofNullable(authorizedClientProviders.get(registrationId))
            .ifPresent(authorizedClientProvider ->
                authorizedClientProvider.deleteAuthorizedClient(principalName));
    }

    public void saveAuthorizedClient(final String registrationId,
                                     final String principalName) {
        ofNullable(getAuthorizedClient(registrationId, principalName))
            .ifPresent(authorizedClient -> ofNullable(authorizedClientProviders.get(registrationId))
                .ifPresent(authorizedClientProvider ->
                    authorizedClientProvider.saveAuthorizedClient(principalName, authorizedClient)));
    }

    protected Map<String, AuthorizedClient> getAuthorizedClientsMap(final String registrationId) {
        return computeIfAbsent(registrationId, key -> ofNullable(authorizedClientProviders.get(registrationId))
            .map(AuthorizedClientProvider::getAuthorizedClientsMap)
            .orElseGet(HashMap::new));
    }
}
