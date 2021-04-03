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

package org.cosinus.swing.preference.dialog;

import org.cosinus.swing.form.Dialog;
import org.cosinus.swing.preference.dialog.DefaultPreferencesDialog;
import org.cosinus.swing.preference.dialog.PreferencesDialogProvider;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;

public class DefaultPreferencesDialogProvider implements PreferencesDialogProvider {

    @Autowired
    private final Translator translator;

    public DefaultPreferencesDialogProvider(Translator translator) {
        this.translator = translator;
    }

    @Override
    public Dialog<Void> getPreferencesDialog(Frame frame) {
        String title = translator.translate("PreferencesForm.title");
        return new DefaultPreferencesDialog(frame, title);
    }

}