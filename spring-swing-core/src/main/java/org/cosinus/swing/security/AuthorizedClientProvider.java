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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cosinus.swing.convert.JsonFileConverter;
import org.cosinus.swing.error.JsonConvertException;
import org.cosinus.swing.resource.FilesystemResourceResolver;
import org.cosinus.swing.resource.ResourceLocator;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import static java.nio.file.Files.isSymbolicLink;
import static java.util.Collections.singleton;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;
import static org.cosinus.swing.resource.ResourceSource.FILESYSTEM;

@Slf4j
public class AuthorizedClientProvider extends JsonFileConverter<AuthorizedClient> {

    public static final String FILE_NAME = "authorizedClients";

    private final FilesystemResourceResolver filesystemResourceResolver;

    @Getter
    private final String storingLocation;

    @Getter
    private final String fileName;

    public AuthorizedClientProvider(final ObjectMapper objectMapper,
                                    final FilesystemResourceResolver filesystemResourceResolver,
                                    final String storingLocation, final String fileName) {
        super(objectMapper, AuthorizedClient.class, singleton(filesystemResourceResolver));
        this.filesystemResourceResolver = filesystemResourceResolver;
        this.storingLocation = storingLocation;
        this.fileName = ofNullable(fileName)
            .filter(name -> !name.isBlank())
            .orElse(FILE_NAME);
    }

    public Map<String, AuthorizedClient> getAuthorizedClientsMap() {
        try {
            File storeFile = filesystemResourceResolver.getFilePath(Paths.get(storingLocation))
                .map(Path::toFile)
                .orElseThrow(() -> new IOException("Failed to resolve storing file " + fileName));

            if (storeFile.exists() && isSymbolicLink(storeFile.toPath())) {
                throw new IOException("Unable to use a symbolic link for storing: " + storeFile);
            }

            // create new file (if necessary)
            Map<String, AuthorizedClient> authorizedClientsMap;
            if (!storeFile.exists()) {
                ofNullable(storeFile.getParentFile())
                    .ifPresent(File::mkdirs);
                return new LinkedHashMap<>();
            }

            return filesystemResourceResolver
                .resolveAllFiles(resourceLocator(), true)
                .collect(toMap(
                    File::getName,
                    userFolder -> convert(FILESYSTEM, userFolder.getName() + "/" + fileName)
                        .orElseGet(AuthorizedClient::new),
                    (userName1, userName2) -> userName1,
                    LinkedHashMap::new));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Optional<AuthorizedClient> convert(String name) {
        try {
            return super.convert(name);
        } catch (JsonConvertException ex) {
            log.error("Failed to read objects from storing file {}", name, ex);
            return empty();
        }
    }

    public void saveAuthorizedClient(final String userName, final AuthorizedClient authorizedClient) {
        try {
            saveModel(userName + "/" + fileName, authorizedClient);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public void deleteAuthorizedClient(String userName) {
        resolveFilesystemPath(userName + "/" + fileName)
            .map(Path::toFile)
            .ifPresent(File::delete);
    }

    @Override
    protected ResourceLocator resourceLocator() {
        return () -> storingLocation;
    }
}
