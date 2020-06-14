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

import org.apache.log4j.Logger;
import org.cosinus.swing.boot.SpringSwingComponent;

import java.awt.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Application preferences
 */
@SpringSwingComponent
public class Preferences {

    private static final Logger LOG = Logger.getLogger(Preferences.class);

    public static final String LOOKANDFEEL = "lookandfeel";
    public static final String LANGUAGE = "language";

    private Map<String, Object> preferencesMap = new HashMap<>();

    public Preferences() {
        LOG.info("No preferences for this application.");
    }

    public Preferences(Map<String, Object> preferencesMap) {
        this.preferencesMap = preferencesMap;
    }

    private Optional<Object> getPreference(String key) {
        return Optional.ofNullable(preferencesMap.get(key));
    }

    public Optional<String> getStringPreference(String key) {
        return getPreference(key).map(Object::toString);
    }

    public Optional<Color> getColorPreference(String key) {
        return getPreference(key)
                .map(Object::toString)
                .map(value -> value.split(","))
                .map(values -> new Color(Integer.parseInt(values[0]),
                                         Integer.parseInt(values[1]),
                                         Integer.parseInt(values[2])));
    }

    public Optional<Font> getFontPreference(String key) {
        return getPreference(key)
                .map(Object::toString)
                .map(value -> value.split(","))
                .map(values -> new Font(values[0],
                                        Integer.parseInt(values[1]),
                                        Integer.parseInt(values[2])));
    }

    public Optional<File> getFilePreference(String key) {
        return getPreference(key)
                .map(Object::toString)
                .map(File::new);
    }

    public Optional<Boolean> getBooleanPreference(String key) {
        return getPreference(key)
                .filter(Boolean.class::isInstance)
                .map(Boolean.class::cast);
    }

    public Optional<Integer> getIntPreference(String key) {
        return getPreference(key)
                .filter(Integer.class::isInstance)
                .map(Integer.class::cast);
    }

    public Color colorPreference(String key) {
        return getColorPreference(key).orElse(null);
    }

    public Font fontPreference(String key) {
        return getFontPreference(key).orElse(null);
    }

    public int intPreference(String key) {
        return getIntPreference(key).orElse(10);
    }

    public boolean booleanPreference(String key) {
        return getBooleanPreference(key).orElse(false);
    }

    public Map<String, Object> getPreferencesMap() {
        return preferencesMap;
    }
}