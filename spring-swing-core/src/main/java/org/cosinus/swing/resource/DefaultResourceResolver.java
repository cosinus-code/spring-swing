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

import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import static org.cosinus.swing.resource.ResourceSource.FILESYSTEM_BEFORE_CLASSPATH;

/**
 * Implementation of {@link ResourceResolver}
 * which try to resolve the resources first in the application classpath
 * and then, if not found, in the filesystem looking in application dedicated folders
 */
public class DefaultResourceResolver implements ResourceResolver {

    private final FilesystemResourceResolver filesystemResourceResolver;

    private final ClasspathResourceResolver classpathResourceResolver;

    public DefaultResourceResolver(FilesystemResourceResolver filesystemResourceResolver,
                                   ClasspathResourceResolver classpathResourceResolver) {
        this.filesystemResourceResolver = filesystemResourceResolver;
        this.classpathResourceResolver = classpathResourceResolver;
    }

    @Override
    public Optional<Path> resolveResourcePath(ResourceLocator resourceLocator, String name) {
        return filesystemResourceResolver.resolveResourcePath(resourceLocator, name)
            .or(() -> classpathResourceResolver.resolveResourcePath(resourceLocator, name));
    }

    @Override
    public Optional<byte[]> resolveAsBytes(ResourceLocator resourceLocator, String name) {
        return filesystemResourceResolver.resolveAsBytes(resourceLocator, name)
            .or(() -> classpathResourceResolver.resolveAsBytes(resourceLocator, name));
    }

    @Override
    public Optional<byte[]> resolveAsBytes(String resourcePath) {
        return filesystemResourceResolver.resolveAsBytes(resourcePath)
            .or(() -> classpathResourceResolver.resolveAsBytes(resourcePath));
    }

    @Override
    public Stream<String> resolveResources(ResourceLocator resourceLocator, String fileExtension) {
        return classpathResourceResolver.resolveResources(resourceLocator, fileExtension);
    }

    @Override
    public ResourceSource getResourceSource() {
        return FILESYSTEM_BEFORE_CLASSPATH;
    }
}
