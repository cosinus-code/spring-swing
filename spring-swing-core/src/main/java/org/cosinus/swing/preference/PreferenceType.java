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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import static java.util.Arrays.stream;

/**
 * Preference type that will be reflected on the real managed value of the preference
 * and function as discriminating for the preference implementation to be instantiated.
 */
public enum PreferenceType {
    TEXT,
    BOOLEAN,
    INTEGER,
    LONG,
    FLOAT,
    DOUBLE,
    LANGUAGE,
    FILE,
    FOLDER,
    COLOR,
    FONT,
    LAF,
    DATE;

    @JsonCreator
    public static PreferenceType toPreferenceType(String value) {
        return stream(values())
            .filter(type -> type.toString().equals(value))
            .findFirst()
            .orElse(null);
    }

    @JsonValue
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
