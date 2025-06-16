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
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.apache.commons.io.IOUtils.toByteArray;
import static org.cosinus.swing.resource.ResourceSource.CLASSPATH;

/**
 * Implementation of {@link ResourceResolver}
 * which try to resolve the resources in the application classpath
 */
public class ClasspathResourceResolver implements ResourceResolver {

    private static final Logger LOG = LogManager.getLogger(ClasspathResourceResolver.class);

    private final ResourcePatternResolver resourceLoader;

    public ClasspathResourceResolver(ResourcePatternResolver resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Resolve a resource path in the application classpath.
     *
     * @param resourceLocator the resource locator
     * @param resourceName    the resource name
     * @return the found resource path, or {@link Optional#empty()}
     */
    @Override
    public Optional<Path> resolveResourcePath(ResourceLocator resourceLocator, String resourceName) {
        return getResourcePath(resourceLocator, resourceName)
            .map(Paths::get);
    }

    /**
     * Resolve a resource as bytes array in the application classpath.
     *
     * @param resourceLocator the resource locator within resource source
     * @param name            the resource name
     * @return the found resource, or {@link Optional#empty()}
     */
    @Override
    public Optional<byte[]> resolveAsBytes(ResourceLocator resourceLocator, String name) {
        return getResourcePath(resourceLocator, name)
            .flatMap(this::resolveAsBytes);
    }

    /**
     * Resolve a resource as bytes array in the application classpath.
     *
     * @param resourcePath the resource path within resource source
     * @return the found resource, or {@link Optional#empty()}
     */
    @Override
    public Optional<byte[]> resolveAsBytes(String resourcePath) {
        Optional<byte[]> bytes = ofNullable(resourcePath)
            .map(path -> path.startsWith("/") ? path : "/" + path)
            .map(path -> {
                try (InputStream input = ClasspathResourceResolver.class.getResourceAsStream(path)) {
                    return input != null ? toByteArray(input) : null;
                } catch (IOException ex) {
                    return null;
                }
            });
        if (bytes.isEmpty()) {
            LOG.debug("Resource not found: {}", resourcePath);
        }
        return bytes;
    }

    /**
     * Resolve resource paths with a specific extension in the application classpath.
     *
     * @param resourceLocator the resource locator within resource source
     * @param fileExtension   the file extension to filter the resources
     * @return the list of found resources
     */
    @Override
    public Stream<String> resolveResources(ResourceLocator resourceLocator, String fileExtension) {
        try {
            Resource[] resources = resourceLoader.getResources(getResourceFolder(resourceLocator) + "**" + fileExtension);
            return Arrays.stream(resources)
                .map(Resource::getFilename);
        } catch (IOException e) {
            LOG.error("Failed to resolve resources of extension: {}", fileExtension);
            return Stream.empty();
        }
    }

    private Optional<String> getResourcePath(ResourceLocator resourceLocator, String resourceName) {
        return ofNullable(resourceName)
            .map(name -> ofNullable(resourceLocator)
                .map(ResourceLocator::getLocation)
                .filter(not(String::isEmpty))
                .map(folder -> folder
                    .concat("/")
                    .concat(name))
                .orElse(name));
    }

    private String getResourceFolder(ResourceLocator resourceLocator) {
        return ofNullable(resourceLocator)
            .map(ResourceLocator::getLocation)
            .map(folder -> folder.concat(File.separator))
            .orElse("");
    }

    @Override
    public ResourceSource getResourceSource() {
        return CLASSPATH;
    }
}
