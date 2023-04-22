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

import java.io.Serial;
import java.util.LinkedHashMap;

/**
 * A group of preferences;
 * <p>
 * This allows to group {@link Preferences} objects per logical sections
 * that will be shown on different panels in the preferences dialog form.
 */
public class PreferencesSet extends LinkedHashMap<String, Preference> {

    @Serial
    private static final long serialVersionUID = -4430681695930280454L;

}
