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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.cosinus.swing.preference.PreferenceType;
import org.cosinus.swing.preference.PrimitivePreference;

import static org.cosinus.swing.preference.PreferenceType.CURRENCY;
import static org.cosinus.swing.preference.PreferenceType.PERCENT;

public class CurrencyPreference extends PrimitivePreference<Double> {

    @Override
    @JsonIgnore
    public PreferenceType getType() {
        return CURRENCY;
    }

}