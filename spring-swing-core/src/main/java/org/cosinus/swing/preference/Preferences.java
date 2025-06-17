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

package org.cosinus.swing.preference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.preference.impl.BooleanPreference;
import org.cosinus.swing.preference.impl.ColorPreference;
import org.cosinus.swing.preference.impl.FilePreference;
import org.cosinus.swing.preference.impl.FontPreference;
import org.cosinus.swing.preference.impl.IntegerPreference;
import org.cosinus.swing.preference.impl.LanguagePreference;
import org.cosinus.swing.preference.impl.LookAndFeelPreference;
import org.cosinus.swing.preference.impl.TextPreference;

import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * Application preferences handler.
 */
public class Preferences {

    private static final Logger LOG = LogManager.getLogger(Preferences.class);

    public static final String LOOK_AND_FEEL = "look-and-feel";
    public static final String LANGUAGE = "language";

    private final Map<String, PreferencesSet> preferenceSetsMap;

    private final Map<String, Preference> preferencesMap;

    public Preferences() {
        LOG.info("No preferences for this application.");
        preferenceSetsMap = new HashMap<>();
        preferencesMap = new HashMap<>();
    }

    public Preferences(Map<String, PreferencesSet> preferenceSetsMap) {
        this.preferenceSetsMap = preferenceSetsMap;
        this.preferencesMap = preferenceSetsMap
            .values()
            .stream()
            .flatMap(preferencesSet -> preferencesSet.entrySet().stream())
            .peek(entry -> entry.getValue().setName(entry.getKey()))
            .collect(toMap(Map.Entry::getKey,
                Map.Entry::getValue));
    }

    public Optional<Preference> findPreference(String key) {
        return ofNullable(preferencesMap.get(key));
    }

    public Optional<String> findStringPreference(String key) {
        return findPreference(TextPreference.class, key);
    }

    public Optional<Locale> findLanguagePreference(String key) {
        return findPreference(LanguagePreference.class, key);
    }

    public Optional<Color> findColorPreference(String key) {
        return findPreference(ColorPreference.class, key);
    }

    public Optional<Font> findFontPreference(String key) {
        return findPreference(FontPreference.class, key);
    }

    public Optional<File> findFilePreference(String key) {
        return findPreference(FilePreference.class, key);
    }

    public Optional<Boolean> findBooleanPreference(String key) {
        return findPreference(BooleanPreference.class, key);
    }

    public Optional<Integer> findIntPreference(String key) {
        return findPreference(IntegerPreference.class, key);
    }

    public <T, R> Optional<R> findPreference(Class<? extends Preference<T, R>> preferenceClass,
                                             String key) {
        return findPreference(key)
            .filter(preferenceClass::isInstance)
            .map(preferenceClass::cast)
            .map(Preference::getRealValue);
    }

    public Color colorPreference(String key) {
        return findColorPreference(key).orElse(null);
    }

    public Font fontPreference(String key) {
        return findFontPreference(key).orElse(null);
    }

    public int intPreference(String key) {
        return findIntPreference(key).orElse(10);
    }

    public boolean booleanPreference(String key) {
        return findBooleanPreference(key).orElse(false);
    }

    public Map<String, Preference> getPreferencesMap() {
        return preferencesMap;
    }

    public Map<String, PreferencesSet> getPreferenceSetsMap() {
        return preferenceSetsMap;
    }

    public void setAvailableLanguages(Collection<Locale> locales) {
        findPreference(LANGUAGE)
            .filter(preference -> preference instanceof LanguagePreference)
            .map(LanguagePreference.class::cast)
            .ifPresent(preference -> preference
                .setRealValues(new ArrayList<>(locales)));
    }

    public void setAvailableLookAndFeels(Collection<LookAndFeelInfo> lookAndFeels) {
        findPreference(LOOK_AND_FEEL)
            .filter(preference -> preference instanceof LookAndFeelPreference)
            .map(LookAndFeelPreference.class::cast)
            .ifPresent(preference -> preference
                .setValues(
                    lookAndFeels
                        .stream()
                        .map(LookAndFeelInfo::getName)
                        .collect(toList())));
    }

    public <R> void updatePreference(String name, R realValue) {
        ofNullable(preferencesMap.get(name))
            .ifPresent(preference -> preference.setRealValue(realValue));
    }
}