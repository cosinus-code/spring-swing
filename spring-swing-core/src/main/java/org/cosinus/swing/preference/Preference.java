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

import com.fasterxml.jackson.annotation.*;
import org.cosinus.swing.preference.impl.*;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

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
    @JsonSubTypes.Type(value = DatePreference.class, name = "date"),
    @JsonSubTypes.Type(value = PercentPreference.class, name = "percent"),
    @JsonSubTypes.Type(value = CurrencyPreference.class, name = "currency")
})
public abstract class Preference<T, R> {

    protected PreferenceType type;

    @JsonIgnore
    protected String name;

    protected T value;

    protected R realValue;

    protected List<R> values;

    public PreferenceType getType() {
        return type;
    }

    public void setType(PreferenceType type) {
        this.type = type;
    }

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

    public List<R> getValues() {
        return values;
    }

    public void setValues(List<R> values) {
        this.values = values;
    }

    @JsonIgnore
    public abstract R getRealValue();

    public abstract void setRealValue(R realValue);
}
