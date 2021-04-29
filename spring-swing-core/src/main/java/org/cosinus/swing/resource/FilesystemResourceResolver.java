/*
 * Copyright 2020 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.swing.resource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.context.ApplicationProperties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static java.nio.file.Files.readAllBytes;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.swing.resource.ResourceSource.FILESYSTEM;

public class FilesystemResourceResolver implements ResourceResolver {

    private static final Logger LOG = LogManager.getLogger(FilesystemResourceResolver.class);

    private final ApplicationProperties applicationProperties;

    public FilesystemResourceResolver(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    @Override
    public Optional<byte[]> resolveImageAsBytes(String name) {
        return resolveAsBytes(ResourceType.IMAGE, name);
    }

    @Override
    public Optional<byte[]> resolveAsBytes(ResourceLocator resourceLocator, String name) {
        return resolveResourcePath(resourceLocator, name)
            .flatMap(this::resolveAsBytes);
    }

    @Override
    public Optional<byte[]> resolveAsBytes(String resourcePath) {
        return resolveAsBytes(Paths.get(resourcePath));
    }

    @Override
    public Optional<Path> resolveResourcePath(ResourceLocator resourceLocator, String name) {
        return getResourcePath(resourceLocator, name)
            .flatMap(this::getFilePath);
    }

    @Override
    public Stream<String> resolveResources(ResourceType type, String fileExtension) {
        return getFilePath(Paths.get(getResourceFolder(type)))
            .map(Path::toFile)
            .filter(File::exists)
            .map(parent -> findFiles(parent, fileExtension))
            .orElseGet(Stream::empty);
    }

    private Optional<byte[]> resolveAsBytes(Path resourcePath) {
        Optional<byte[]> bytes = ofNullable(resourcePath)
            .flatMap(this::getFilePath)
            .flatMap(this::fileToBytes);
        if (bytes.isEmpty()) {
            LOG.debug("Resource not found: " + resourcePath);
        }
        return bytes;
    }

    public Optional<Path> getResourcePath(ResourceLocator resourceLocator, String resourceName) {
        return ofNullable(resourceName)
            .map(name -> ofNullable(resourceLocator)
                .map(ResourceLocator::getLocation)
                .filter(not(String::isEmpty))
                .map(folder -> Paths.get(folder, name))
                .orElse(Paths.get(name)));
    }

    private Optional<Path> getFilePath(Path resourcePath) {
        return ofNullable(applicationProperties.getHome())
            .map(Paths::get)
            .or(() -> getApplicationFolderName()
                .map(applicationFolderName -> Paths.get(System.getProperty("user.home"), applicationFolderName)))
            .map(applicationHomePath -> applicationHomePath.resolve(resourcePath));
    }

    public Optional<String> getApplicationFolderName() {
        return ofNullable(applicationProperties.getName())
            .map(String::toLowerCase)
            .map("."::concat);
    }

    private String getResourceFolder(ResourceType type) {
        return ofNullable(type)
            .map(ResourceType::name)
            .map(String::toLowerCase)
            .map(folder -> folder.concat(File.separator))
            .orElse("");
    }

    private Stream<String> findFiles(File parent, String fileExtension) {
        return ofNullable(parent.listFiles((isDir, name) -> name.endsWith(fileExtension)))
            .stream()
            .flatMap(Arrays::stream)
            .map(File::getName);
    }

    private Optional<byte[]> fileToBytes(String path) {
        return ofNullable(path)
            .map(File::new)
            .filter(File::exists)
            .map(File::toPath)
            .flatMap(this::fileToBytes);
    }

    private Optional<byte[]> fileToBytes(Path filePath) {
        try {
            return Optional.of(readAllBytes(filePath));
        } catch (IOException ex) {
            return Optional.empty();
        }
    }

    @Override
    public ResourceSource getResourceSource() {
        return FILESYSTEM;
    }
}
