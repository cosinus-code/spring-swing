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

import org.cosinus.swing.form.control.ComboBox;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Stream.concat;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class LookAndFeelControl extends ComboBox<String> {

    private static final String DEFAULT_LOOK_AND_FEEL = "Default";

    public LookAndFeelControl(List<String> values, String value) {
        super(concat(Stream.of(DEFAULT_LOOK_AND_FEEL), values.stream()).toArray(String[]::new), value);
    }

    @Override
    public String getControlValue() {
        String value = super.getControlValue();
        return DEFAULT_LOOK_AND_FEEL.equals(value) ? "" : value;
    }

    @Override
    public void setControlValue(String value) {
        super.setControlValue(isEmpty(value) ? DEFAULT_LOOK_AND_FEEL : value);
    }
}
