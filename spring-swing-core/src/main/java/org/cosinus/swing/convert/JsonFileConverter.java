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
import org.cosinus.swing.error.JsonConvertException;
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
 * Abstract class for converting json conf file into model.
 * <p>
 * It provides handy tools for converting json files to models
 * and for saving models into json files.
 * <p>
 * The json files are identified by names adn are search using different strategies:
 * <ul>
 * <li>FILESYSTEM: search in filesystem looking in the locations dedicated to the this application</li>
 * <li>CLASSPATH: search in the application classpath</li>
 * <li>FILESYSTEM_BEFORE_CLASSPATH: search first in filesystem and next, if not found, in the classpath</li>
 * </ul>
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

    /**
     * Convert a json file into a model.
     * <p>
     * It uses the default resource resolver defined by FILESYSTEM_BEFORE_CLASSPATH to search for the file.
     *
     * @param name the name to identify the json file
     * @return the converted model, or {@link Optional#empty()}
     */
    public Optional<T> convert(String name) {
        return convert(FILESYSTEM_BEFORE_CLASSPATH, name, this::toModel);
    }

    /**
     * Convert a json file into a list of models.
     * <p>
     * It uses the default resource resolver defined by FILESYSTEM_BEFORE_CLASSPATH to search for the file.
     *
     * @param name the name to identify the json file
     * @return the list of models, or {@link Optional#empty()}
     */
    public Optional<List<T>> convertToListOfModels(String name) {
        return convert(FILESYSTEM_BEFORE_CLASSPATH, name, this::toModelsList);
    }

    /**
     * Convert a json file into a map of models.
     * <p>
     * It uses the default resource resolver defined by FILESYSTEM_BEFORE_CLASSPATH to search for the file.
     *
     * @param name the name to identify the json file
     * @return the map of models, or {@link Optional#empty()}
     */
    public Optional<Map<String, T>> convertToMapOfModels(String name) {
        return convert(FILESYSTEM_BEFORE_CLASSPATH, name, this::toModelsMap);
    }

    /**
     * Convert a json file into a model.
     * <p>
     * It uses the given resource source to search for the file.
     *
     * @param resourceSource the resource source to search for the file
     * @param name           the name to identify the json file
     * @return the converted model, or {@link Optional#empty()}
     */
    public Optional<T> convert(ResourceSource resourceSource, String name) {
        return convert(resourceSource, name, this::toModel);
    }

    /**
     * Convert a json file into a list of model.
     * <p>
     * It uses the given resource source to search for the file.
     *
     * @param resourceSource the resource source to search for the file
     * @param name           the name to identify the json file
     * @return the converted list of models, or {@link Optional#empty()}
     */
    public Optional<List<T>> convertToListOfModels(ResourceSource resourceSource, String name) {
        return convert(resourceSource, name, this::toModelsList);
    }

    /**
     * Convert a json file into a map of model.
     * <p>
     * It uses the given resource source to search for the file.
     *
     * @param resourceSource the resource source to search for the file
     * @param name           the name to identify the json file
     * @return the converted map of models, or {@link Optional#empty()}
     */
    public Optional<Map<String, T>> convertToMapOfModels(ResourceSource resourceSource, String name) {
        return convert(resourceSource, name, this::toModelsMap);
    }

    /**
     * Convert a json file into a model.
     * <p>
     * It uses the given resource source to search for the file.
     *
     * @param resourceSource the resource source to search for the file
     * @param name           the name to identify the json file
     * @param mapper         the mapper used to convert the bytes array read from to file to the model
     * @param <P>            the type of the model
     * @return the converted model, or {@link Optional#empty()}
     */
    public <P> Optional<P> convert(ResourceSource resourceSource, String name, Function<byte[], P> mapper) {
        return ofNullable(resourceResolversMap.get(resourceSource))
            .flatMap(resourceResolver -> resourceResolver.resolveAsBytes(resourceLocator(), getFileName(name)))
            .map(mapper);
    }

    /**
     * Get the json filename corresponding to name.
     *
     * @param name the name
     * @return the filename
     */
    protected String getFileName(String name) {
        return name.endsWith(JSON_EXTENSION) ? name : name + JSON_EXTENSION;
    }

    /**
     * Converts a json bytes array to a model.
     *
     * @param bytes the bytes to convert
     * @return the converted model
     */
    protected T toModel(byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, modelClass);
        } catch (IOException e) {
            throw new JsonConvertException(format("The json file doesn't contain the expected model of type %s: %s",
                                                  modelClass,
                                                  new String(bytes)), e);
        }
    }

    /**
     * Converts a json bytes array to a list of models.
     *
     * @param bytes the bytes to convert
     * @return the converted list of models
     */
    protected List<T> toModelsList(byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, objectMapper
                .getTypeFactory()
                .constructParametricType(List.class, modelClass));
        } catch (IOException e) {
            throw new JsonConvertException(format("The json file doesn't contain the expected list of models of type %s: %s",
                                                  modelClass,
                                                  new String(bytes)), e);
        }
    }

    /**
     * Converts a json bytes array to a map of models.
     *
     * @param bytes the bytes to convert
     * @return the converted map of models
     */
    protected Map<String, T> toModelsMap(byte[] bytes) {
        try {
            return objectMapper.readValue(bytes, objectMapper
                .getTypeFactory()
                .constructParametricType(Map.class, String.class, modelClass));
        } catch (IOException e) {
            throw new JsonConvertException(format("The json file doesn't contain the expected json map of models of type %s: %s",
                                                  modelClass,
                                                  new String(bytes)), e);
        }
    }

    /**
     * Saves a model to json file.
     *
     * @param name the name to identify the json file
     * @param model the model to save
     * @throws IOException if an IO error occurs
     */
    public void saveModel(String name, T model) throws IOException {
        try (OutputStream output = createOutputStream(name)) {
            writeValue(output, model);
        }
    }

    /**
     * Saves a list of models to json file.
     *
     * @param name   the name to identify the json file
     * @param models the list of models to save
     * @throws IOException if an IO error occurs
     */
    public void saveModelsList(String name, List<T> models) throws IOException {
        try (OutputStream output = createOutputStream(name)) {
            writeValue(output, models);
        }
    }

    /**
     * Saves a map of models to json file.
     *
     * @param name      the name to identify the json file
     * @param modelsMap the map of models to save
     * @throws IOException if an IO error occurs
     */
    public void saveModelsMap(String name, Map<String, T> modelsMap) throws IOException {
        try (OutputStream output = createOutputStream(name)) {
            writeValue(output, modelsMap);
        }
    }

    /**
     * Create an output stream for a file identified by a name.
     *
     * @param name the name to identify the file
     * @return the created output stream
     * @throws IOException if an IO error occurs
     */
    protected OutputStream createOutputStream(String name) throws IOException {
        File file = resourceResolversMap.get(FILESYSTEM).resolveResourcePath(resourceLocator(), name)
            .map(Path::toFile)
            .orElseThrow(() -> new IOException("No application home folder (probably due to missing application name) " +
                                                   "to save file: " + name));
        if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
            throw new IOException("Failed to create folders for file: " + file);
        }
        return new FileOutputStream(file);
    }

    /**
     * Write a model an output stream as json.
     *
     * @param output the output stream to write to
     * @param model  the model to write
     * @throws IOException if an IO error occurs
     */
    protected void writeValue(OutputStream output, Object model) throws IOException {
        objectMapper
            .writerWithDefaultPrettyPrinter()
            .writeValue(output, model);
    }

    /**
     * Get resource locator to locate the resource within the resource source.
     * <p>
     * Usually, this is an inner folder (like "conf/", "i18n/", "images/", etc...)
     * used to group the resources within application resource source.
     *
     * @return the resource locator
     */
    protected abstract ResourceLocator resourceLocator();

}
