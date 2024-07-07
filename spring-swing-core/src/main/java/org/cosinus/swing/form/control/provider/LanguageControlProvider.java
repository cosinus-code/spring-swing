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

package org.cosinus.swing.form.control.provider;

import org.cosinus.swing.form.control.Control;
import org.cosinus.swing.form.control.LanguageControl;

import java.util.Locale;

import static java.util.Collections.singletonList;

/**
 * Implementation of {@link ControlProvider}
 * for providing the {@link Control} corresponding to a language value.
 */
public class LanguageControlProvider implements ControlProvider<Locale> {

    @Override
    public LanguageControl getControl() {
        Locale defaultLocal = Locale.getDefault();
        return new LanguageControl(singletonList(defaultLocal), defaultLocal);
    }

    @Override
    public <T> LanguageControl getControl(ControlDescriptor<T, Locale> descriptor) {
        return new LanguageControl(descriptor.getRealValues(), descriptor.getRealValue());
    }
}
