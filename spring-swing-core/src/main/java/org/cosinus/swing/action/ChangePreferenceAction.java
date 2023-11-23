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

package org.cosinus.swing.action;

import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.preference.PreferencesProvider;

import java.io.IOException;
import java.io.UncheckedIOException;

public abstract class ChangePreferenceAction implements ActionInContext {

    protected final Preferences preferences;

    protected final PreferencesProvider preferencesProvider;

    protected ChangePreferenceAction(Preferences preferences,
                                     PreferencesProvider preferencesProvider) {
        this.preferences = preferences;
        this.preferencesProvider = preferencesProvider;
    }

    @Override
    public void run(ActionContext context) {
        preferences.updatePreference(getPreferenceName(), getPreferenceNewValue());
        try {
            preferencesProvider.savePreferences(preferences);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        apply();
    }

    protected abstract String getPreferenceName();

    protected abstract Object getPreferenceNewValue();

    protected abstract void apply();
}
