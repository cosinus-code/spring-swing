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

package org.cosinus.swing.boot.properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

public class PropertiesConfigurationFile implements EnvironmentPostProcessor {

    public static final String USER_DIR = "user.dir";
    public static final String APPLICATION_DIR = "application.dir";

    @Override
    public void postProcessEnvironment(final ConfigurableEnvironment environment,
                                       final SpringApplication application) {
        Stream.of(
                environment.getProperty(APPLICATION_DIR),
                System.getProperty(APPLICATION_DIR),
                System.getProperty(USER_DIR))
            .filter(Objects::nonNull)
            .flatMap(applicationDir -> Stream.of(
                    "application.properties",
                    "application.yml",
                    "application.json")
                .map(propertiesFileName -> Paths.get(applicationDir, propertiesFileName)))
            .map(Path::toFile)
            .filter(File::exists)
            .filter(File::isFile)
            .map(File::getAbsolutePath)
            .map("file:"::concat)
            .map(this::getResourcePropertySource)
            .forEach(propertySource ->
                environment.getPropertySources().addAfter("random", propertySource));
    }

    private ResourcePropertySource getResourcePropertySource(final String resourceLocation) {
        try {
            return new ResourcePropertySource(resourceLocation);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
