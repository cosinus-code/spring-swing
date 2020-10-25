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

package org.cosinus.swing.preference;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cosinus.swing.resource.ResourceResolver;
import org.cosinus.swing.convert.JsonFileConverter;
import org.cosinus.swing.resource.ResourceType;

import java.util.Map;
import java.util.Optional;

/**
 * Preferences provider from json file
 */
public class JsonPreferencesProvider extends JsonFileConverter<Map> implements PreferencesProvider {

    private static final String PREFERENCES_FILE_NAME = "preferences.json";

    public JsonPreferencesProvider(ObjectMapper objectMapper,
                                   ResourceResolver resourceResolver) {
        super(objectMapper, Map.class, resourceResolver);
    }

    public Optional<Preferences> getPreferences() {
        return convert(PREFERENCES_FILE_NAME)
                .map(Preferences::new);
    }

    @Override
    protected ResourceType resourceLocator() {
        return ResourceType.CONF;
    }
}