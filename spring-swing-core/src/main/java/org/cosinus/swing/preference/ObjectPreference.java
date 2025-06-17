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

package org.cosinus.swing.preference;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

/**
 * Abstract class for {@link Preference} managing not primitive values.
 * <p>
 * For these preferences, the saved value is always a string
 *
 * @param <R> the type of the managed preference value
 */
public abstract class ObjectPreference<R> extends Preference<String, R> {

    @Override
    public R getRealValue() {
        return toRealValue(value);
    }

    @Override
    public void setRealValue(R realValue) {
        setValue(fromRealValue(realValue));
    }

    @Override
    public List<R> getRealValues() {
        return getValues()
            .stream()
            .map(this::toRealValue)
            .collect(Collectors.toList());
    }

    @Override
    public void setRealValues(List<R> values) {
        setValues(values
            .stream()
            .map(this::fromRealValue)
            .collect(Collectors.toList()));
    }

    @JsonIgnore
    protected String fromRealValue(R realValue) {
        return ofNullable(realValue)
            .map(Object::toString)
            .orElse(null);
    }

    @JsonIgnore
    protected abstract R toRealValue(String value);
}
