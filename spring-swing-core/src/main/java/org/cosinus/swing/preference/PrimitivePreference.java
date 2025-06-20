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

import java.util.List;

/**
 * Abstract class for {@link Preference} managing primitive values.
 * <p>
 * For these preferences, the saved value and the managed value are the same type
 *
 * @param <T> the type of both saved and managed preference values
 */
public abstract class PrimitivePreference<T> extends Preference<T, T> {

    @Override
    public List<T> getRealValues() {
        return getValues();
    }

    @Override
    public void setRealValues(List<T> values) {
        setValues(values);
    }

    @Override
    public T getRealValue() {
        return value;
    }

    @Override
    public void setRealValue(T realValue) {
        setValue(realValue);
    }
}
