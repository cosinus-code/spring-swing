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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.convert.JsonFileConverter;
import org.cosinus.swing.error.JsonConvertException;
import org.cosinus.swing.resource.FilesystemResourceResolver;
import org.cosinus.swing.resource.ResourceLocator;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.nio.file.Files.isSymbolicLink;
import static java.util.Collections.singleton;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.cosinus.swing.resource.ResourceSource.FILESYSTEM;

public class LocalStorageOAuth2AuthorizedClientService extends JsonFileConverter<AuthorizedClient>
    implements OAuth2AuthorizedClientService {

    private static final Logger LOG = LogManager.getLogger(LocalStorageOAuth2AuthorizedClientService.class);

    public static final String FILE_NAME = "authorizedClients";

    private final ClientRegistrationRepository clientRegistrationRepository;

    private final String storingLocation;

    private final Map<String, AuthorizedClient> authorizedClientsMap;

    public LocalStorageOAuth2AuthorizedClientService(final ObjectMapper objectMapper,
                                                     final FilesystemResourceResolver filesystemResourceResolver,
                                                     final ClientRegistrationRepository clientRegistrationRepository,
                                                     final String storingLocation) {
        super(objectMapper, AuthorizedClient.class, singleton(filesystemResourceResolver));
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.storingLocation = storingLocation;

        try {
            File storeFile = filesystemResourceResolver.getFilePath(Paths.get(storingLocation))
                .map(Path::toFile)
                .orElseThrow(() -> new IOException("Failed to resolve storing file " + FILE_NAME));

            if (storeFile.exists() && isSymbolicLink(storeFile.toPath())) {
                throw new IOException("Unable to use a symbolic link for storing: " + storeFile);
            }

            // create new file (if necessary)
            if (!storeFile.exists()) {
                ofNullable(storeFile.getParentFile())
                    .ifPresent(File::mkdirs);
                authorizedClientsMap = new LinkedHashMap<>();
            } else {
                authorizedClientsMap = filesystemResourceResolver
                    .resolveAllFiles(resourceLocator(), true)
                    .collect(toMap(
                        File::getName,
                        userFolder -> convert(FILESYSTEM, userFolder.getName() + "/" + FILE_NAME)
                            .orElseGet(AuthorizedClient::new),
                        (userName1, userName2) -> userName1,
                        LinkedHashMap::new));
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<Map<String, AuthorizedClient>> convertToMapOfModels(String name) {
        try {
            return super.convertToMapOfModels(name);
        } catch (JsonConvertException ex) {
            LOG.error("Failed to read objects from storing file {}", name, ex);
            return empty();
        }
    }

    @Override
    public DetailedOAuth2AuthorizedClient loadAuthorizedClient(String clientRegistrationId, String principalName) {
        return ofNullable(authorizedClientsMap.get(principalName))
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

        ofNullable(authorizedClientsMap.get(principal.getName()))
            .map(AuthorizedClient::getDetails)
            .stream()
            .flatMap(existingDetails -> existingDetails.entrySet().stream())
            .filter(entry -> ofNullable(details.get(entry.getKey()))
                .map(newValue -> !newValue.equals(entry.getValue()))
                .orElse(true))
            .forEach(entry -> details.put(entry.getKey(), entry.getValue()));
        authorizedClient.setDetails(details);

        authorizedClientsMap.put(principal.getName(), authorizedClient);
        save(principal.getName());
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
        authorizedClientsMap.remove(principalName);
        resolveFilesystemPath(principalName + "/" + FILE_NAME)
            .map(Path::toFile)
            .ifPresent(File::delete);
    }

    public void save(String principalName) {
        ofNullable(authorizedClientsMap.get(principalName))
            .ifPresent(authorizedClient -> {
                try {
                    saveModel(principalName + "/" + FILE_NAME, authorizedClient);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
    }

    public Map<String, AuthorizedClient> getAuthorizedClientsMap() {
        return authorizedClientsMap;
    }

    @Override
    protected ResourceLocator resourceLocator() {
        return () -> storingLocation;
    }
}

