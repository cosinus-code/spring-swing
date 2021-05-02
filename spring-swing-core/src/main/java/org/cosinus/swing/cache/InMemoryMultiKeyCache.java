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

package org.cosinus.swing.cache;

import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;

import java.util.Optional;

import static java.util.Optional.ofNullable;

/**
 * In memory hash map {@link MultiKeyCache} based implementation for a {@link MultiKeyCache} interface
 */
public class InMemoryMultiKeyCache<T> extends MultiKeyMap<Object, T> implements MultiKeyCache<T> {

    private static final long serialVersionUID = 1970762625157829923L;

    @Override
    public T cache(T value, Object... keys) {
        put(key(keys), value);
        return value;
    }

    @Override
    public Optional<T> getValue(Object... keys) {
        return ofNullable(get(key(keys)));
    }

    private MultiKey<Object> key(Object... keys) {
        return new MultiKey<>(keys);
    }
}
