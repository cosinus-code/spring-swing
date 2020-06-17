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

package org.cosinus.swing.store;

import org.cosinus.swing.context.SpringSwingComponent;

import java.util.prefs.Preferences;

import static java.lang.String.join;

/**
 * Implementation of {@link ApplicationStorage} using local preferences
 */
@SpringSwingComponent
public class LocalApplicationStorage implements ApplicationStorage {

    private static final String KEY_DELIMITER = ".";

    private final Preferences userPreferences;

    public LocalApplicationStorage(Class<?> appClass) {
        userPreferences = Preferences.userNodeForPackage(appClass);
    }

    @Override
    public String getString(String key) {
        return userPreferences.get(key, null);
    }

    @Override
    public void saveString(String key, String value) {
        userPreferences.put(key, value);
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return userPreferences.getInt(key, defaultValue);
    }

    @Override
    public void saveInt(String key, int value) {
        userPreferences.putInt(key, value);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return userPreferences.getInt(key, defaultValue ? 1 : 0) == 1;
    }

    @Override
    public void saveBoolean(String key, boolean value) {
        userPreferences.putInt(key, value ? 1 : 0);
    }

    @Override
    public String key(String... keys) {
        return join(KEY_DELIMITER, keys);
    }
}
