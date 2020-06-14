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

import java.io.IOException;
import java.util.Optional;

import static java.lang.String.format;

/**
 * Abstract class for converting json conf file into model
 */
public abstract class JsonFileConverter<T> {

    private final ObjectMapper objectMapper;

    private final Class<T> modelClass;

    private final ResourceResolver resourceResolver;

    protected JsonFileConverter(ObjectMapper objectMapper,
                                Class<T> modelClass,
                                ResourceResolver resourceResolver) {
        this.objectMapper = objectMapper;
        this.modelClass = modelClass;
        this.resourceResolver = resourceResolver;
    }

    public Optional<T> convert(String name) {
        String filename = name.endsWith(".json") ? name : name + ".json";
        return resourceResolver.resolveAsBytes(resourceLocator(), filename)
                .map(this::toModel);
    }

    private T toModel(byte[] bytes) {
        try {
            return objectMapper.readValue(bytes,
                                          modelClass);
        } catch (IOException e) {
            throw new RuntimeException(format("The file doesn't contain a expected json model: %s",
                                              new String(bytes)), e);
        }
    }

    protected ResourceLocator resourceLocator() {
        return null;
    }
}
