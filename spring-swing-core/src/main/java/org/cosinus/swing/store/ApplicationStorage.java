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

import static java.lang.String.join;
import static java.util.Optional.ofNullable;

/**
 * Interface for storage of application related data
 */
public interface ApplicationStorage {

    String KEY_DELIMITER = ".";

    /**
     * Get a stored string value.
     *
     * @param key the key to search for
     * @return the stored value, or null
     */
    String getString(String key);

    /**
     * Save a string value in the storage.
     *
     * @param key   the key of the value to save
     * @param value the value to save
     */
    void saveString(String key, String value);

    /**
     * Get a stored integer value.
     *
     * @param key the key to search for
     * @return the stored value, or defaultValue
     */
    int getInt(String key, int defaultValue);

    /**
     * Save an integer value in the storage.
     *
     * @param key   the key of the value to save
     * @param value the value to save
     */
    void saveInt(String key, int value);

    /**
     * Get a stored boolean value.
     *
     * @param key the key to search for
     * @return the stored value, or defaultValue
     */
    boolean getBoolean(String key, boolean defaultValue);

    /**
     * Save a boolean value in the storage.
     *
     * @param key   the key of the value to save
     * @param value the value to save
     */
    void saveBoolean(String key, boolean value);

    /**
     * Remove a value from storage.
     *
     * @param key the key of value to remove
     */
    void remove(String key);

    /**
     * Clear the whole storage. All values will be removed.
     */
    void clear();

    /**
     * Save an object in the storage.
     * <p>
     * The actual value stored is the returned {@link Object#toString()} value.
     *
     * @param key    the key of the value to save
     * @param object the object to save
     */
    default void save(String key, Object object) {
        String value = ofNullable(object)
            .map(Object::toString)
            .orElse(null);
        saveString(key, value);
    }

    /**
     * Create a combined key from an array of sub-keys.
     *
     * @param keys the sub-keys
     * @return the created key
     */
    default String key(String... keys) {
        return join(KEY_DELIMITER, keys);
    }
}
