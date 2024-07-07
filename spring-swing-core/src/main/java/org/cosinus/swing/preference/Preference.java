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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.cosinus.swing.form.control.ControlType;
import org.cosinus.swing.form.control.provider.ControlDescriptor;
import org.cosinus.swing.preference.impl.BooleanPreference;
import org.cosinus.swing.preference.impl.ColorPreference;
import org.cosinus.swing.preference.impl.DatePreference;
import org.cosinus.swing.preference.impl.DoublePreference;
import org.cosinus.swing.preference.impl.FilePreference;
import org.cosinus.swing.preference.impl.FloatPreference;
import org.cosinus.swing.preference.impl.FolderPreference;
import org.cosinus.swing.preference.impl.FontPreference;
import org.cosinus.swing.preference.impl.IntegerPreference;
import org.cosinus.swing.preference.impl.LanguagePreference;
import org.cosinus.swing.preference.impl.LongPreference;
import org.cosinus.swing.preference.impl.LookAndFeelPreference;
import org.cosinus.swing.preference.impl.TextPreference;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

/**
 * Abstract class for the application preferences.
 * <p>
 * The type property is discriminating for sub-type preferences.
 *
 * @param <T> the type of preference value to be saved as json value
 * @param <R> the type of the real preference value to be handled
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextPreference.class, name = "text"),
    @JsonSubTypes.Type(value = BooleanPreference.class, name = "boolean"),
    @JsonSubTypes.Type(value = IntegerPreference.class, name = "integer"),
    @JsonSubTypes.Type(value = LongPreference.class, name = "long"),
    @JsonSubTypes.Type(value = FloatPreference.class, name = "float"),
    @JsonSubTypes.Type(value = DoublePreference.class, name = "double"),
    @JsonSubTypes.Type(value = LanguagePreference.class, name = "language"),
    @JsonSubTypes.Type(value = FilePreference.class, name = "file"),
    @JsonSubTypes.Type(value = FolderPreference.class, name = "folder"),
    @JsonSubTypes.Type(value = ColorPreference.class, name = "color"),
    @JsonSubTypes.Type(value = FontPreference.class, name = "font"),
    @JsonSubTypes.Type(value = LookAndFeelPreference.class, name = "laf"),
    @JsonSubTypes.Type(value = DatePreference.class, name = "date")
})
public abstract class Preference<T, R> implements ControlDescriptor<T, R> {

    protected ControlType type;

    protected String name;

    protected T value;

    protected List<T> values;

    @JsonIgnore
    public ControlType getType() {
        return type;
    }

    public void setType(ControlType type) {
        this.type = type;
    }

    @JsonIgnore
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public List<T> getValues() {
        return values;
    }

    public void setValues(List<T> values) {
        this.values = values;
    }

    @JsonIgnore
    @Override
    public abstract R getRealValue();

    public abstract void setRealValue(R realValue);

    @JsonIgnore
    @Override
    public abstract List<R> getRealValues();

    public abstract void setRealValues(List<R> values);
}
