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

package org.cosinus.swing.image.icon;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

import javax.swing.*;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Hash map {@link IconsCache} implementation
 */
public class DefaultIconsCache extends MultiKeyMap<String, Icon> implements IconsCache {

    @Override
    public Icon cache(Icon icon, String name) {
        return cache(icon, name, null, false);
    }

    @Override
    public Icon cache(Icon icon, String name, IconSize size) {
        return cache(icon, name, size, false);
    }

    @Override
    public Icon cache(Icon icon, String name, IconSize size, boolean hidden) {
        put(key(name, size, hidden), icon);
        return icon;
    }

    @Override
    public Optional<Icon> getIcon(String name) {
        return getIcon(name, null, false);
    }

    @Override
    public Optional<Icon> getIcon(String name, IconSize size) {
        return getIcon(name, size, false);
    }

    @Override
    public Optional<Icon> getIcon(String name, IconSize size, boolean hidden) {
        return Optional.ofNullable(get(key(name, size, hidden)));
    }

    private MultiKey<String> key(String name,
                                 IconSize size,
                                 boolean hidden) {
        return key(name,
                   Optional.ofNullable(size)
                           .map(IconSize::name)
                           .orElse(null),
                   hidden ? "hidden" : null);
    }

    private MultiKey<String> key(String name,
                                 String size,
                                 String hidden) {
        String[] key = Stream.of(name, size, hidden)
                .filter(Objects::nonNull)
                .toArray(String[]::new);

        return new MultiKey(key);
    }
}
