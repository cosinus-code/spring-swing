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

package org.cosinus.swing.preference.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.cosinus.swing.preference.ObjectPreference;
import org.cosinus.swing.preference.Preference;
import org.cosinus.swing.form.control.ControlType;

import java.util.Locale;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.form.control.ControlType.LANGUAGE;

/**
 * Implementation of {@link Preference} for language managed values.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
public class LanguagePreference extends ObjectPreference<Locale> {

    @Override
    public ControlType getType() {
        return LANGUAGE;
    }

    @Override
    public Locale toRealValue(String value) {
        return ofNullable(value)
            .map(Locale::new)
            .orElse(null);
    }
}
