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

package org.cosinus.swing.convert;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cosinus.swing.resource.ResourceLocator;
import org.cosinus.swing.resource.ResourceResolver;
import org.cosinus.swing.resource.ResourceSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.resource.ResourceSource.FILESYSTEM;
import static org.cosinus.swing.resource.ResourceSource.FILESYSTEM_BEFORE_CLASSPATH;

/**
 * Abstract class for converting json conf file into model
 */
public abstract class JsonFileConverter<T> {

    private static final String JSON_EXTENSION = ".json";

    private final ObjectMapper objectMapper;

    private final Class<T> modelClass;

    private final Map<ResourceSource, ResourceResolver> resourceResolversMap;

    protected JsonFileConverter(ObjectMapper objectMapper,
                                Class<T> modelClass,
                                Set<ResourceResolver> resourceResolvers) {
        this.objectMapper = objectMapper;
        this.modelClass = modelClass;
        this.resourceResolversMap = resourceResolvers
            .stream()
            .collect(Collectors.toMap(ResourceResolver::getResourceSource,
                                      Function.identity()));
    }

    public Optional<T> convert(String name) {
        return convert(FILESYSTEM_BEFORE_CLASSPATH, name, this::toModel);
    }

    public Optional<List<T>> convertToListOfModels(String name) {
        return convert(FILESYSTEM_BEFORE_CLASSPATH, name, this::toModelsList);
    }

    public Optional<Map<String, T>> convertToMapOfModels(String name) {
        return convert(FILESYSTEM_BEFORE_CLASSPATH, name, this::toModelsMap);
    }

    public Optional<T> convert(ResourceSource resourceSource, String name) {
        return convert(resourceSource, name, this::toModel);
    }

    public Optional<List<T>> convertToListOfModels(ResourceSource resourceSource, String name) {
        return convert(resourceSource, name, this::toModelsList);
    }

    public Optional<Map<String, T>> convertToMapOfModels(ResourceSource resourceSource, String name) {
        return convert(resourceSource, name, this::toModelsMap);
    }

    public <P> Optional<P> convert(ResourceSource resourceSource, String name, Function<byte[], P> mapper) {
        return ofNullable(resourceResolversMap.get(resourceSource))
            .flatMap(resourceResolver -> resourceResolver.resolveAsBytes(resourceLocator(), getFileName(name)))
            .map(mapper);
    }

    protected String getFileName(String name) {
        return name.endsWith(JSON_EXTENSION) ? name : name + JSON_EXTENSION;
    }

    protected T toModel(byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, modelClass);
        } catch (IOException e) {
            throw new RuntimeException(format("The json file doesn't contain the expected model of type %s: %s",
                                              modelClass,
                                              new String(bytes)), e);
        }
    }

    protected List<T> toModelsList(byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, objectMapper
                .getTypeFactory()
                .constructParametricType(List.class, modelClass));
        } catch (IOException e) {
            throw new RuntimeException(format("The json file doesn't contain the expected list of models of type %s: %s",
                                              modelClass,
                                              new String(bytes)), e);
        }
    }

    protected Map<String, T> toModelsMap(byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, objectMapper
                .getTypeFactory()
                .constructParametricType(Map.class, String.class, modelClass));
        } catch (IOException e) {
            throw new RuntimeException(format("The json file doesn't contain the expected json map of models of type %s: %s",
                                              modelClass,
                                              new String(bytes)), e);
        }
    }

    public void saveModel(String name, T model) throws IOException {
        try (OutputStream output = createOutputStream(name)) {
            writeValue(output, model);
        }
    }

    public void saveModelsList(String name, List<T> models) throws IOException {
        try (OutputStream output = createOutputStream(name)) {
            writeValue(output, models);
        }
    }

    public void saveModelsMap(String name, Map<String, T> modelsMap) throws IOException {
        try (OutputStream output = createOutputStream(name)) {
            writeValue(output, modelsMap);
        }
    }

    private OutputStream createOutputStream(String name) throws IOException {
        File file = resourceResolversMap.get(FILESYSTEM).resolveResourcePath(resourceLocator(), name)
            .map(Path::toFile)
            .orElseThrow(() -> new IOException("No application home folder (probably due to missing application name) " +
                                                   "to save file: " + name));
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new IOException("Failed to create folders for file: " + file);
        }
        return new FileOutputStream(file);
    }

    private void writeValue(OutputStream output, Object value) throws IOException {
        objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValue(output, value);
    }

    protected abstract ResourceLocator resourceLocator();

}
