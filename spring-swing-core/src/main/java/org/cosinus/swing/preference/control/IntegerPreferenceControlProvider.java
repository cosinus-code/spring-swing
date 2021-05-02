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

package org.cosinus.swing.preference.control;

import org.cosinus.swing.form.control.Control;
import org.cosinus.swing.form.control.Spinner;
import org.cosinus.swing.preference.Preference;

import static java.util.Optional.ofNullable;

/**
 * Implementation of {@link PreferenceControlProvider}
 * for providing the {@link Control} corresponding to a integer preference value.
 */
public class IntegerPreferenceControlProvider implements PreferenceControlProvider<Integer> {

    @Override
    public <T> Control<Integer> getPreferenceControl(Preference<T, Integer> preference) {
        return new Spinner<>(ofNullable(preference.getRealValue()).orElse(0));
    }
}
