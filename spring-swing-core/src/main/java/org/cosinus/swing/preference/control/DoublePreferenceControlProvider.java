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
import org.cosinus.swing.form.control.FormattedTextField;
import org.cosinus.swing.preference.Preference;

import static java.text.NumberFormat.getNumberInstance;

public class DoublePreferenceControlProvider implements PreferenceControlProvider<Double> {

    @Override
    public <T> Control<Double> getPreferenceControl(Preference<T, Double> preference) {
        FormattedTextField<Double> control = new FormattedTextField<>(getNumberInstance(), Double.class);
        control.setValue(preference.getRealValue());
        control.setColumns(10);
        return control;
    }

}
