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

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.WinReg.HKEY;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static com.sun.jna.platform.win32.Advapi32Util.registryGetKeys;
import static com.sun.jna.platform.win32.Advapi32Util.registryGetValues;
import static com.sun.jna.platform.win32.WinReg.HKEY_CLASSES_ROOT;
import static com.sun.jna.platform.win32.WinReg.HKEY_CURRENT_USER;

/**
 * {@link Registry} implementation using JNA.
 */
@Slf4j
public class JnaRegistry implements Registry {

    @Override
    public List<String> getKeys(final RegistryRoot root, final String key) {
        return read(root, key, (hkey, path) -> List.of(registryGetKeys(hkey, path)), List.of());
    }

    @Override
    public Map<String, Object> getValues(final RegistryRoot root, final String key) {
        return read(root, key,
            (hkey, path) -> new LinkedHashMap<>(registryGetValues(hkey, path)),
            Map.of());
    }

    @Override
    public boolean keyExists(final RegistryRoot root, final String key) {
        return read(root, key, (hkey, path) -> true, false);
    }

    @Override
    public Optional<Object> getValue(final RegistryRoot root, final String key, final String name) {
        return read(root, key,
            (hkey, path) -> Advapi32Util.registryValueExists(hkey, path, name) ?
                Optional.ofNullable(Advapi32Util.registryGetValue(hkey, path, name)) :
                Optional.empty(),
            Optional.empty());
    }

    @Override
    public void setStringValue(final RegistryRoot root, final String key, final String name, final String value) {
        HKEY hkey = toHkey(root);
        Advapi32Util.registryCreateKey(hkey, key);
        Advapi32Util.registrySetStringValue(hkey, key, name, value);
    }

    @Override
    public boolean deleteKey(final RegistryRoot root, final String key) {
        HKEY hkey = toHkey(root);
        try {
            if (Advapi32Util.registryKeyExists(hkey, key)) {
                Advapi32Util.registryDeleteKey(hkey, key);
            }
            return true;
        } catch (RuntimeException ex) {
            log.debug("Cannot delete the registry key {} \\ {}", root, key, ex);
            return false;
        }
    }

    private HKEY toHkey(final RegistryRoot root) {
        return root == RegistryRoot.CURRENT_USER ? HKEY_CURRENT_USER : HKEY_CLASSES_ROOT;
    }

    private <T> T read(final RegistryRoot root,
                       final String key,
                       final BiFunction<HKEY, String, T> reader,
                       final T defaultValue) {
        HKEY hkey = toHkey(root);
        try {
            return Advapi32Util.registryKeyExists(hkey, key) ? reader.apply(hkey, key) : defaultValue;
        } catch (RuntimeException | LinkageError ex) {
            log.debug("Cannot read the registry key {} \\ {}", root, key, ex);
            return defaultValue;
        }
    }
}
