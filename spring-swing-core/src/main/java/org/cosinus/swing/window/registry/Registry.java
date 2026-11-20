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
package org.cosinus.swing.window.registry;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Access to the Windows registry.
 */
public interface Registry {

    /**
     * The per-user file extensions settings of Windows Explorer, under {@link RegistryRoot#CURRENT_USER}.
     */
    String REGISTRY_FILE_EXTS = "Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\";

    /**
     * The per-user file associations, under {@link RegistryRoot#CURRENT_USER}.
     */
    String REGISTRY_USER_CLASSES = "Software\\Classes\\";

    /**
     * The user choice of the default application of an extension, under {@link #REGISTRY_FILE_EXTS}.
     */
    String REGISTRY_USER_CHOICE = "\\UserChoice";

    /**
     * The applications registered by executable name, under {@link RegistryRoot#CLASSES_ROOT}.
     */
    String REGISTRY_APPLICATIONS = "Applications\\";

    /**
     * The name of the default value of a key.
     */
    String REGISTRY_DEFAULT_VALUE = "";

    /**
     * The ProgIDs which can open an extension, as value names, under an extension key.
     */
    String REGISTRY_OPEN_WITH_PROGIDS = "\\OpenWithProgids";

    /**
     * The executable names which can open an extension, under an extension key:
     * as values under {@link #REGISTRY_FILE_EXTS}, as sub-keys under {@link RegistryRoot#CLASSES_ROOT}.
     */
    String REGISTRY_OPEN_WITH_LIST = "\\OpenWithList";

    /**
     * The value of an "OpenWithList" key with the order of the other values, not an executable name.
     */
    String REGISTRY_MRU_LIST = "MRUList";

    /**
     * @return the sub-key names of a key, or empty if the key doesn't exist
     */
    List<String> getKeys(RegistryRoot root, String key);

    /**
     * @return the values of a key by name, or empty if the key doesn't exist
     */
    Map<String, Object> getValues(RegistryRoot root, String key);

    /**
     * @return true if the key exists
     */
    boolean keyExists(RegistryRoot root, String key);

    /**
     * Get a value of a key: a {@link String} for the string values (not expanded),
     * an {@link Integer} for DWORD, a {@link Long} for QWORD, a String[] for the multi-strings,
     * a byte[] for the binary values.
     *
     * @param name the value name, or {@link #REGISTRY_DEFAULT_VALUE} for the default value of the key
     * @return the value, or {@link Optional#empty()} if the key or the value doesn't exist
     */
    Optional<Object> getValue(RegistryRoot root, String key, String name);

    /**
     * Get a value of a key as string: the string values as they are, the numbers in decimal.
     *
     * @param name the value name, or {@link #REGISTRY_DEFAULT_VALUE} for the default value of the key
     * @return the value, or {@link Optional#empty()} if not found or not a string or number value
     */
    default Optional<String> getStringValue(final RegistryRoot root, final String key, final String name) {
        return getValue(root, key, name)
            .map(value -> value instanceof Number number ? String.valueOf(number) : value)
            .filter(String.class::isInstance)
            .map(String.class::cast);
    }

    /**
     * Get a value of a key as boolean: true only for the number 1, as a number or as a (hexadecimal) string.
     *
     * @return the value, or false if not found
     */
    default boolean getBooleanValue(final RegistryRoot root, final String key, final String name) {
        return getValue(root, key, name)
            .map(Registry::isOne)
            .orElse(false);
    }

    /**
     * Get the default value of a key as string.
     *
     * @param path the full path of the key, e.g. "HKCR\\.txt"
     * @return the value, or {@link Optional#empty()} if not found
     * @see #getStringValue(RegistryRoot, String, String)
     */
    default Optional<String> getStringValue(final String path) {
        return getStringValue(path, REGISTRY_DEFAULT_VALUE);
    }

    /**
     * Get a value of a key as string.
     *
     * @param path the full path of the key, e.g. "HKCR\\.txt"
     * @param name the value name, or {@link #REGISTRY_DEFAULT_VALUE} for the default value of the key
     * @return the value, or {@link Optional#empty()} if not found or the path has an unsupported root
     * @see #getStringValue(RegistryRoot, String, String)
     */
    default Optional<String> getStringValue(final String path, final String name) {
        return RegistryRoot.fromPath(path)
            .flatMap(root -> getStringValue(root, RegistryRoot.keyOf(path), name));
    }

    /**
     * Get a value of a key as boolean.
     *
     * @param path the full path of the key, e.g. "HKCU\\Software\\Microsoft"
     * @return the value, or false if not found or the path has an unsupported root
     * @see #getBooleanValue(RegistryRoot, String, String)
     */
    default boolean getBooleanValue(final String path, final String name) {
        return RegistryRoot.fromPath(path)
            .map(root -> getBooleanValue(root, RegistryRoot.keyOf(path), name))
            .orElse(false);
    }

    /**
     * Set a string value, creating the key if needed.
     *
     * @param name the value name, or {@link #REGISTRY_DEFAULT_VALUE} for the default value of the key
     */
    void setStringValue(RegistryRoot root, String key, String name, String value);

    /**
     * Delete a key without sub-keys, if it exists.
     *
     * @return true if the key doesn't exist anymore
     */
    boolean deleteKey(RegistryRoot root, String key);

    private static boolean isOne(final Object value) {
        if (value instanceof Number number) {
            return number.longValue() == 1;
        }
        if (value instanceof String text) {
            try {
                return Long.decode(text.trim()) == 1;
            } catch (NumberFormatException ex) {
                return false;
            }
        }
        return false;
    }
}
