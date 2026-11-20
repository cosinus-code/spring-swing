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
package org.cosinus.swing.test.window.registry;

import org.cosinus.swing.window.registry.Registry;
import org.cosinus.swing.window.registry.RegistryRoot;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.cosinus.swing.window.registry.Registry.REGISTRY_DEFAULT_VALUE;
import static org.cosinus.swing.window.registry.RegistryRoot.CLASSES_ROOT;
import static org.cosinus.swing.window.registry.RegistryRoot.CURRENT_USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The registry paths and the conversions of the registry values, independent of the registry implementation.
 */
public class RegistryTest {

    private static final String PERSONALIZE = "Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize";

    private final MapRegistry registry = new MapRegistry();

    @Test
    public void testRootFromPath() {
        assertEquals(Optional.of(CURRENT_USER), RegistryRoot.fromPath("HKCU\\Software"));
        assertEquals(Optional.of(CURRENT_USER), RegistryRoot.fromPath("HKEY_CURRENT_USER\\Software"));
        assertEquals(Optional.of(CLASSES_ROOT), RegistryRoot.fromPath("hkcr\\.txt"));
        assertEquals(Optional.of(CLASSES_ROOT), RegistryRoot.fromPath("HKCR"));
        assertEquals(Optional.empty(), RegistryRoot.fromPath("HKLM\\Software"));
        assertEquals(Optional.empty(), RegistryRoot.fromPath(null));
    }

    @Test
    public void testKeyOfPath() {
        assertEquals("Software\\Microsoft", RegistryRoot.keyOf("HKCU\\Software\\Microsoft"));
        assertEquals(".txt", RegistryRoot.keyOf("HKCR\\.txt"));
        assertEquals("", RegistryRoot.keyOf("HKCR"));
    }

    @Test
    public void testGetStringValue() {
        registry.put(CLASSES_ROOT, ".txt", REGISTRY_DEFAULT_VALUE, "txtfile");
        registry.put(CLASSES_ROOT, ".txt", "PerceivedType", "text");
        registry.put(CLASSES_ROOT, ".txt", "Count", 5);
        registry.put(CLASSES_ROOT, ".txt", "Data", new byte[]{1});

        assertEquals(Optional.of("txtfile"), registry.getStringValue("HKCR\\.txt"));
        assertEquals(Optional.of("text"), registry.getStringValue("HKCR\\.txt", "PerceivedType"));
        assertEquals(Optional.of("5"), registry.getStringValue("HKCR\\.txt", "Count"));
        assertEquals(Optional.empty(), registry.getStringValue("HKCR\\.txt", "Data"));
        assertEquals(Optional.empty(), registry.getStringValue("HKCR\\.txt", "Missing"));
        assertEquals(Optional.empty(), registry.getStringValue("HKCR\\.log"));
        assertEquals(Optional.empty(), registry.getStringValue("HKLM\\.txt"));
    }

    @Test
    public void testGetBooleanValue() {
        registry.put(CURRENT_USER, PERSONALIZE, "DwordOne", 1);
        registry.put(CURRENT_USER, PERSONALIZE, "DwordZero", 0);
        registry.put(CURRENT_USER, PERSONALIZE, "HexOne", "0x1");
        registry.put(CURRENT_USER, PERSONALIZE, "Text", "yes");

        assertTrue(registry.getBooleanValue("HKCU\\" + PERSONALIZE, "DwordOne"));
        assertFalse(registry.getBooleanValue("HKCU\\" + PERSONALIZE, "DwordZero"));
        assertTrue(registry.getBooleanValue("HKCU\\" + PERSONALIZE, "HexOne"));
        assertFalse(registry.getBooleanValue("HKCU\\" + PERSONALIZE, "Text"));
        assertFalse(registry.getBooleanValue("HKCU\\" + PERSONALIZE, "Missing"));
        assertFalse(registry.getBooleanValue("HKLM\\" + PERSONALIZE, "DwordOne"));
    }

    /**
     * {@link Registry} with the values in memory, implementing only the value reading.
     */
    private static class MapRegistry implements Registry {

        private final Map<String, Object> values = new HashMap<>();

        void put(final RegistryRoot root, final String key, final String name, final Object value) {
            values.put(root + "|" + key + "|" + name, value);
        }

        @Override
        public Optional<Object> getValue(final RegistryRoot root, final String key, final String name) {
            return Optional.ofNullable(values.get(root + "|" + key + "|" + name));
        }

        @Override
        public List<String> getKeys(final RegistryRoot root, final String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, Object> getValues(final RegistryRoot root, final String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean keyExists(final RegistryRoot root, final String key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setStringValue(final RegistryRoot root, final String key, final String name, final String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean deleteKey(final RegistryRoot root, final String key) {
            throw new UnsupportedOperationException();
        }
    }
}
