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

/**
 * Implementation of {@link ResourceResolver}
 * which try to resolve the resources in the filesystem looking the application dedicated locations,
 * usually in "[user.home]/.[application.name]" folder
 */
public class FilesystemResourceResolver implements ResourceResolver {

    private static final Logger LOG = LogManager.getLogger(FilesystemResourceResolver.class);

    private final ApplicationProperties applicationProperties;

    public FilesystemResourceResolver(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Resolve a resource path from the filesystem.
     *
     * @param resourceLocator the resource locator
     * @param name            the resource name
     * @return the found resource path, or {@link Optional#empty()}
     */
    @Override
    public Optional<Path> resolveResourcePath(ResourceLocator resourceLocator, String name) {
        return getResourcePath(resourceLocator, name)
            .flatMap(this::getFilePath);
    }

    /**
     * Resolve a resource as bytes array from the filesystem.
     *
     * @param resourceLocator the resource locator within resource source
     * @param name            the resource name
     * @return the found resource, or {@link Optional#empty()}
     */
    @Override
    public Optional<byte[]> resolveAsBytes(ResourceLocator resourceLocator, String name) {
        return resolveResourcePath(resourceLocator, name)
            .flatMap(this::resolveAsBytes);
    }

    /**
     * Resolve a resource as bytes array from the filesystem.
     *
     * @param resourcePath the resource path within resource source
     * @return the found resource, or {@link Optional#empty()}
     */
    @Override
    public Optional<byte[]> resolveAsBytes(String resourcePath) {
        return resolveAsBytes(Paths.get(resourcePath));
    }

    /**
     * Resolve resource paths with a specific extension from the filesystem.
     *
     * @param resourceLocator the resource locator within resource source
     * @param fileExtension   the file extension to filter the resources
     * @return the list of found resources
     */
    @Override
    public Stream<String> resolveResources(ResourceLocator resourceLocator, String fileExtension) {
        return getFilePath(Paths.get(getResourceFolder(resourceLocator)))
            .map(Path::toFile)
            .filter(File::exists)
            .map(parent -> findFiles(parent, fileExtension))
            .orElseGet(Stream::empty);
    }

    public Stream<File> resolveAllFiles(ResourceLocator resourceLocator, Boolean onlyDirs) {
        return getFilePath(Paths.get(getResourceFolder(resourceLocator)))
            .map(Path::toFile)
            .filter(File::exists)
            .map(parent -> ofNullable(parent.listFiles((file, name) -> ofNullable(onlyDirs)
                .map(dirs -> dirs == file.isDirectory())
                .orElse(true)))
                .stream()
                .flatMap(Arrays::stream))
            .orElseGet(Stream::empty);
    }

    /**
     * Get the resource path within the resource source.
     *
     * @param resourceLocator the resource locator
     * @param resourceName    the resource name
     * @return the found resource path, or {@link Optional#empty()}
     */
    public Optional<Path> getResourcePath(ResourceLocator resourceLocator, String resourceName) {
        return ofNullable(resourceName)
            .map(name -> ofNullable(resourceLocator)
                .map(ResourceLocator::getLocation)
                .filter(not(String::isEmpty))
                .map(folder -> Paths.get(folder, name.split("/")))
                .orElse(Paths.get(name)));
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

    public Optional<Path> getFilePath(Path resourcePath) {
        return ofNullable(applicationProperties.getHome())
            .map(Paths::get)
            .or(() -> getApplicationFolderName()
                .map(applicationFolderName -> Paths.get(System.getProperty("user.home"), applicationFolderName)))
            .map(applicationHomePath -> applicationHomePath.resolve(resourcePath));
    }

    private Optional<String> getApplicationFolderName() {
        return ofNullable(applicationProperties.getName())
            .map(String::toLowerCase)
            .map("."::concat);
    }

    protected String getResourceFolder(ResourceLocator resourceLocator) {
        return ofNullable(resourceLocator)
            .map(ResourceLocator::getLocation)
            .map(folder -> folder.concat(File.separator))
            .orElse("");
    }

    private Stream<String> findFiles(File parent, String fileExtension) {
        return ofNullable(parent.listFiles((isDir, name) -> ofNullable(fileExtension)
            .map(name::endsWith)
            .orElse(true)))
            .stream()
            .flatMap(Arrays::stream)
            .map(File::getName);
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
