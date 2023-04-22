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

import java.io.Serial;
import java.util.HashMap;

import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link ApplicationStorage} using in-memory map (used for testing purposes).
 */
public class InMemoryApplicationStorage extends HashMap<String, String> implements ApplicationStorage {

    @Serial
    private static final long serialVersionUID = -3680442662776410600L;

    @Override
    public String getString(String key) {
        return get(key);
    }

    @Override
    public void saveString(String key, String value) {
        if (value != null) {
            put(key, value);
        }
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return ofNullable(get(key))
            .map(Integer::parseInt)
            .orElse(defaultValue);
    }

    @Override
    public void saveInt(String key, int value) {
        put(key, Integer.toString(value));
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return ofNullable(get(key))
            .map(Boolean::parseBoolean)
            .orElse(defaultValue);
    }

    @Override
    public void saveBoolean(String key, boolean value) {
        put(key, Boolean.toString(value));
    }

    @Override
    public void remove(String key) {
        super.remove(key);
    }
}
