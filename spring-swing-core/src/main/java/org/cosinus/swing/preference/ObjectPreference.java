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

package org.cosinus.swing.preference;

import static java.util.Optional.ofNullable;

/**
 * Abstract class for {@link Preference} managing not primitive values.
 *
 * For these preferences, the saved value is always a string
 *
 * @param <R> the type of the managed preference value
 */
public abstract class ObjectPreference<R> extends Preference<String, R> {

    @Override
    public void setRealValue(R realValue) {
        setValue(ofNullable(realValue)
                     .map(Object::toString)
                     .orElse(null));
    }
}
