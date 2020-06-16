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
import org.cosinus.swing.boot.ApplicationProperties;
import org.cosinus.swing.boot.SpringSwingComponent;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static java.nio.file.Files.readAllBytes;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.io.IOUtils.toByteArray;

@SpringSwingComponent
public class ResourceResolver {

    private static final Logger LOG = LogManager.getLogger(ResourceResolver.class);

    private final ResourcePatternResolver resourceLoader;

    private final ApplicationProperties applicationProperties;

    public ResourceResolver(ResourcePatternResolver resourceLoader,
                            ApplicationProperties applicationProperties) {
        this.resourceLoader = resourceLoader;
        this.applicationProperties = applicationProperties;
    }

    public Optional<byte[]> resolveImageAsBytes(String name) {
        return resolveAsBytes(ResourceType.IMAGE, name);
    }

    public Optional<byte[]> resolveAsBytes(ResourceLocator resourceLocator, String name) {
        return ofNullable(getResourcePath(resourceLocator, name))
                .flatMap(this::resolveAsBytes);
    }

    public Optional<byte[]> resolveAsBytes(String resourcePath) {
        Optional<byte[]> bytes = ofNullable(resourcePath)
                .flatMap(path -> resolveAsBytesFromFilesystem(path)
                        .or(() -> resolveAsBytesFromClasspath(path)));
        if (bytes.isEmpty()) {
            LOG.info("Resource not found: " + resourcePath);
        }
        return bytes;
    }

    public Optional<byte[]> resolveAsBytesFromClasspath(String resourcePath) {
        return ofNullable(resourcePath)
                .map(path -> path.startsWith("/") ? path : "/" + path)
                .map(path -> {
                    try (InputStream input = ResourceResolver.class.getResourceAsStream(path)) {
                        return input != null ? toByteArray(input) : null;
                    } catch (IOException ex) {
                        return null;
                    }
                });
    }

    public Optional<byte[]> resolveAsBytesFromFilesystem(String resourcePath) {
        return getFilePath(resourcePath)
                .flatMap(this::fileToBytes);
    }

    private Optional<Path> getFilePath(String resourceName) {
        String applicationHome = ofNullable(applicationProperties.getHome())
                .orElseGet(() -> System.getProperty("user.dir"));
        return ofNullable(applicationHome)
                .map(Paths::get)
                .map(path -> path.resolve(resourceName));
    }

    public String getResourcePath(ResourceLocator resourceLocator, String name) {
        if (name == null) {
            return null;
        }

        return ofNullable(resourceLocator)
                .map(ResourceLocator::getLocation)
                .filter(not(String::isEmpty))
                .map(folder -> folder
                        .concat("/")
                        .concat(name))
                .orElse(name);
    }

    public Stream<String> resolveResources(ResourceType type, String fileExtension) {
        return getFilePath(getResourceFolder(type))
                .map(Path::toFile)
                .filter(File::exists)
                .map(parent -> findFiles(parent, fileExtension))
                .orElseGet(() -> findResources(type, fileExtension));
    }

    private Stream<String> findResources(ResourceType type,
                                         String fileExtension) {
        try {
            Resource[] resources = resourceLoader.getResources(getResourceFolder(type) + "**" + fileExtension);
            return Arrays.stream(resources)
                    .map(Resource::getFilename);
        } catch (IOException e) {
            return Stream.empty();
        }
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

    public static Optional<byte[]> resourceAsBytes(String path) throws IOException {
        File file;
        try {
            file = new ClassPathResource(path).getFile();
        } catch (IOException ex) {
            return Optional.empty();
        }

        try (InputStream inputStream = new FileInputStream(file)) {
            return ofNullable(toByteArray(inputStream));
        }
    }

    public static Optional<File> resourceAsFile(String name) {
        return ofNullable(name)
                .map(ResourceResolver.class.getClassLoader()::getResource)
                .map(URL::getFile)
                .map(File::new);
    }
}
