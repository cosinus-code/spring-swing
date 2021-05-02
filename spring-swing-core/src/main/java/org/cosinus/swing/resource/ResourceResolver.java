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

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Resource resolver interface.
 */
public interface ResourceResolver {

    /**
     * Resolve a resource path given the resource locator and the resource name.
     *
     * @param resourceLocator the resource locator
     * @param name            the resource name
     * @return the found resource path, or {@link Optional#empty()}
     */
    Optional<Path> resolveResourcePath(ResourceLocator resourceLocator, String name);

    /**
     * Resolve a resource as bytes array.
     *
     * @param resourceLocator the resource locator within resource source
     * @param name            the resource name
     * @return the found resource, or {@link Optional#empty()}
     */
    Optional<byte[]> resolveAsBytes(ResourceLocator resourceLocator, String name);

    /**
     * Resolve a resource as bytes array.
     *
     * @param resourcePath the resource path within resource source
     * @return the found resource, or {@link Optional#empty()}
     */
    Optional<byte[]> resolveAsBytes(String resourcePath);

    /**
     * Resolve resource paths with a specific extension.
     *
     * @param resourceLocator the resource locator within resource source
     * @param fileExtension   the file extension to filter the resources
     * @return the list of found resources
     */
    Stream<String> resolveResources(ResourceLocator resourceLocator, String fileExtension);

    /**
     * Get the resource source of this resolver.
     *
     * @return the resource source
     */
    ResourceSource getResourceSource();

    /**
     * Resolve an image resource.
     *
     * @param name the image resource name
     * @return the found image resource, or {@link Optional#empty()}
     */
    default Optional<byte[]> resolveImageAsBytes(String name) {
        return resolveAsBytes(ResourceType.IMAGE, name);
    }
}
