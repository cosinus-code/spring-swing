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
