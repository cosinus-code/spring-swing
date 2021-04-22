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

package org.cosinus.swing.image.icon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.ofNullable;

public class SectionedProperties extends HashMap<String, Map<String, String>> {

    public static final char SEPARATOR = '=';
    public static final String COMMENT_MARKER = "#";

    public SectionedProperties() {

    }

    public SectionedProperties(InputStream input) throws IOException {
        load(input);
    }

    public SectionedProperties load(InputStream input) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, UTF_8))) {
            return load(reader);
        }
    }

    protected SectionedProperties load(BufferedReader reader) throws IOException {
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

    public Optional<String> getProperty(String sectionName, String propertyName) {
        return ofNullable(get(sectionName))
            .map(section -> section.get(propertyName));
    }
}
