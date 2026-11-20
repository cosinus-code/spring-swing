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

import java.util.Optional;

import static java.util.Arrays.stream;

/**
 * The roots of the Windows registry used by {@link Registry}.
 * <p>
 * A registry path starts with the root, in short ("HKCU") or long form ("HKEY_CURRENT_USER"),
 * followed by the key, e.g. "HKCU\\Software\\Microsoft".
 */
public enum RegistryRoot {
    CURRENT_USER("HKCU", "HKEY_CURRENT_USER"),
    CLASSES_ROOT("HKCR", "HKEY_CLASSES_ROOT");

    public static final String KEY_SEPARATOR = "\\";

    private final String shortName;

    private final String longName;

    RegistryRoot(final String shortName, final String longName) {
        this.shortName = shortName;
        this.longName = longName;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    /**
     * Find the root of a registry path.
     *
     * @param path the registry path, e.g. "HKCU\\Software\\Microsoft"
     * @return the root, or {@link Optional#empty()} if the path doesn't start with a supported root
     */
    public static Optional<RegistryRoot> fromPath(final String path) {
        return Optional.ofNullable(path)
            .map(RegistryRoot::rootName)
            .flatMap(rootName -> stream(values())
                .filter(root -> root.shortName.equalsIgnoreCase(rootName) || root.longName.equalsIgnoreCase(rootName))
                .findFirst());
    }

    /**
     * Get the key of a registry path, without the root.
     *
     * @param path the registry path, e.g. "HKCU\\Software\\Microsoft"
     * @return the key, e.g. "Software\\Microsoft", or empty for the root itself
     */
    public static String keyOf(final String path) {
        int separatorIndex = path.indexOf(KEY_SEPARATOR);
        return separatorIndex >= 0 ? path.substring(separatorIndex + 1) : "";
    }

    private static String rootName(final String path) {
        int separatorIndex = path.indexOf(KEY_SEPARATOR);
        return separatorIndex >= 0 ? path.substring(0, separatorIndex) : path;
    }
}
