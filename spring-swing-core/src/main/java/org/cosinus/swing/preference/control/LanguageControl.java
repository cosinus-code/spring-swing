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
import org.cosinus.swing.preference.impl.DisplayLocale;

import javax.swing.*;
import java.util.List;
import java.util.Locale;

import static java.util.Optional.ofNullable;

public class LanguageControl extends JComboBox<DisplayLocale> implements Control<Locale> {

    public LanguageControl(List<Locale> values, Locale value) {
        super(values.stream()
                  .map(DisplayLocale::new)
                  .toArray(DisplayLocale[]::new));
        setValue(value);
    }

    @Override
    public Locale getValue() {
        return ofNullable(super.getSelectedItem())
            .map(DisplayLocale.class::cast)
            .map(DisplayLocale::getLocale)
            .orElse(null);
    }

    @Override
    public void setValue(Locale locale) {
        setSelectedItem(new DisplayLocale(locale));
    }
}
