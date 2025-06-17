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

package org.cosinus.swing.form.control;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.cosinus.swing.form.control.provider.*;

import static java.util.Arrays.stream;

/**
 * Control type that will be reflected on the real managed value of the control
 * and function as discriminating for the control implementation to be instantiated.
 */
public enum ControlType {
    TEXT(new TextControlProvider()),
    BOOLEAN(new BooleanControlProvider()),
    INTEGER(new IntegerControlProvider()),
    LONG(new LongControlProvider()),
    FLOAT(new FloatControlProvider()),
    DOUBLE(new DoubleControlProvider()),
    LANGUAGE(new LanguageControlProvider()),
    LAF(new LookAndFeelControlProvider()),
    FILE(new FileControlProvider()),
    FOLDER(new FolderControlProvider()),
    COLOR(new ColorControlProvider()),
    FONT(new FontControlProvider()),
    DATE(new DateControlProvider()),
    COMBOBOX(new ComboboxControlProvider()),
    CHECKBOX(new CheckboxControlProvider());


    private final ControlProvider controlProvider;

    ControlType(ControlProvider controlProvider) {
        this.controlProvider = controlProvider;
    }

    @JsonCreator
    public static ControlType fromValue(String value) {
        return stream(values())
            .filter(type -> type.toString().equals(value))
            .findFirst()
            .orElse(null);
    }

    @JsonValue
    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public ControlProvider getControlProvider() {
        return controlProvider;
    }
}
