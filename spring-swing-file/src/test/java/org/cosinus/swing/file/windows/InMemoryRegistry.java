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
package org.cosinus.swing.file.windows;

import org.cosinus.swing.window.registry.Registry;
import org.cosinus.swing.window.registry.RegistryRoot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.cosinus.swing.window.shell.WindowsShellWindowsApi.ASSOCF_INIT_IGNOREUNKNOWN;
import static org.cosinus.swing.window.shell.WindowsShellWindowsApi.ASSOCSTR_PROGID;

/**
 * {@link Registry} in memory, recording the writes.
 * <p>
 * As Windows does when there is no user choice, a per-user association written in the registry
 * becomes the default association of the {@link InMemoryFileAssociationQuery}.
 */
public class InMemoryRegistry implements Registry {

    private final InMemoryFileAssociationQuery fileAssociationQuery;

    private final Map<String, Map<String, Object>> values = new HashMap<>();

    private final Map<String, List<String>> keys = new HashMap<>();

    private final Set<String> existingKeys = new HashSet<>();

    private final Set<String> protectedKeys = new HashSet<>();

    private final List<String> writes = new ArrayList<>();

    public InMemoryRegistry(final InMemoryFileAssociationQuery fileAssociationQuery) {
        this.fileAssociationQuery = fileAssociationQuery;
    }

    /**
     * Forget all keys, values and writes.
     */
    public void reset() {
        values.clear();
        keys.clear();
        existingKeys.clear();
        protectedKeys.clear();
        writes.clear();
    }

    public void putValues(final RegistryRoot root, final String key, final Map<String, Object> keyValues) {
        values.put(path(root, key), keyValues);
    }

    public void putKeys(final RegistryRoot root, final String key, final List<String> subKeys) {
        keys.put(path(root, key), subKeys);
    }

    public void addKey(final RegistryRoot root, final String key) {
        existingKeys.add(path(root, key));
    }

    /**
     * Add a key which cannot be deleted, like the protected "UserChoice" keys.
     */
    public void addProtectedKey(final RegistryRoot root, final String key) {
        addKey(root, key);
        protectedKeys.add(path(root, key));
    }

    /**
     * @return the writes, as "set root+key|name|value" and "delete root+key"
     */
    public List<String> getWrites() {
        return writes;
    }

    @Override
    public List<String> getKeys(final RegistryRoot root, final String key) {
        return keys.getOrDefault(path(root, key), List.of());
    }

    @Override
    public Map<String, Object> getValues(final RegistryRoot root, final String key) {
        return values.getOrDefault(path(root, key), Map.of());
    }

    @Override
    public Optional<Object> getValue(final RegistryRoot root, final String key, final String name) {
        return Optional.ofNullable(getValues(root, key).get(name));
    }

    @Override
    public boolean keyExists(final RegistryRoot root, final String key) {
        return existingKeys.contains(path(root, key));
    }

    @Override
    public void setStringValue(final RegistryRoot root, final String key, final String name, final String value) {
        writes.add("set " + path(root, key) + "|" + name + "|" + value);
        existingKeys.add(path(root, key));
        if (root == RegistryRoot.CURRENT_USER && key.startsWith(REGISTRY_USER_CLASSES) && name.isEmpty()) {
            fileAssociationQuery.put(ASSOCF_INIT_IGNOREUNKNOWN, ASSOCSTR_PROGID,
                key.substring(REGISTRY_USER_CLASSES.length()), value);
        }
    }

    @Override
    public boolean deleteKey(final RegistryRoot root, final String key) {
        writes.add("delete " + path(root, key));
        if (protectedKeys.contains(path(root, key))) {
            return false;
        }
        existingKeys.remove(path(root, key));
        return true;
    }

    /**
     * @return the path of a key, as written in {@link #getWrites()}
     */
    public static String path(final RegistryRoot root, final String key) {
        return root + key;
    }
}
