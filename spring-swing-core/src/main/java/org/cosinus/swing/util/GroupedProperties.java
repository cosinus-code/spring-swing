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

package org.cosinus.swing.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;

/**
 * Grouped properties model.
 * <p>
 * This mimics a {@link java.util.Properties} model
 * with the difference that the properties are grouped on sections
 * specified in lines of format
 * <p>
 * [Group_Name]
 * <p>
 * before the lines corresponding to the grouped properties.
 */
public class GroupedProperties extends HashMap<String, Map<String, String>> {

    @Serial
    private static final long serialVersionUID = -9044321314711472913L;

    public static final char SEPARATOR = '=';
    public static final String COMMENT_MARKER = "#";

    public GroupedProperties() {

    }

    public GroupedProperties(InputStream input) throws IOException {
        load(input);
    }

    /**
     * Load grouped properties from an input stream.
     *
     * @param input the input stream to read from
     * @return the loaded grouped properties
     * @throws IOException if an IO error occurs
     */
    public GroupedProperties load(InputStream input) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8))) {
            return load(reader);
        }
    }

    /**
     * Load grouped properties from a reader.
     *
     * @param reader the reader to read from
     * @return the loaded grouped properties
     * @throws IOException if an IO error occurs
     */
    protected GroupedProperties load(BufferedReader reader) throws IOException {
        String line;
        String sectionName = null;
        while ((line = reader.readLine()) != null) {
            if (!line.trim().isEmpty() && !line.startsWith(COMMENT_MARKER)) {
                if (line.startsWith("[") && line.endsWith("]")) {
                    sectionName = line.substring(1, line.length() - 1);
                    put(sectionName, new HashMap<>());
                } else {
                    int separatorIndex = line.indexOf(SEPARATOR);
                    if (separatorIndex > 0) {
                        String key = line.substring(0, separatorIndex);
                        String value = line.substring(separatorIndex + 1);
                        ofNullable(get(sectionName))
                            .ifPresent(sectionMap -> sectionMap.put(key, value));
                    }
                }
            }
        }

        return this;
    }

    /**
     * Get a property value.
     *
     * @param groupName    the group name to search the property
     * @param propertyName the name of the property to search
     * @return the found property vale, or {@link Optional#empty()}
     */
    public Optional<String> getProperty(String groupName, String propertyName) {
        return ofNullable(get(groupName))
            .map(section -> section.get(propertyName));
    }
}
