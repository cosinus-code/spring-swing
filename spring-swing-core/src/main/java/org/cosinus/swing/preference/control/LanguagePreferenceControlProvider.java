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
import org.cosinus.swing.form.control.LanguageControl;
import org.cosinus.swing.preference.Preference;

import java.util.Locale;

/**
 * Implementation of {@link PreferenceControlProvider}
 * for providing the {@link Control} corresponding to a language preference value.
 */
public class LanguagePreferenceControlProvider implements PreferenceControlProvider<Locale> {

    @Override
    public <T> Control<Locale> getPreferenceControl(Preference<T, Locale> preference) {
        return new LanguageControl(preference.getValues(), preference.getRealValue());
    }
}
