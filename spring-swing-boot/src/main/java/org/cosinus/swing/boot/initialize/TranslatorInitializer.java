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

package org.cosinus.swing.boot.initialize;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.translate.Translator;

import java.util.Locale;

import static org.cosinus.swing.preference.Preferences.LANGUAGE;

public class TranslatorInitializer implements ApplicationInitializer {

    private static final Logger LOG = LogManager.getLogger(TranslatorInitializer.class);

    private final Preferences preferences;

    private final Translator translator;

    public TranslatorInitializer(Preferences preferences,
                                 Translator translator) {
        this.preferences = preferences;
        this.translator = translator;
    }

    @Override
    public void initialize() {
        Locale locale = preferences.findLanguagePreference(LANGUAGE)
            .orElseGet(Locale::getDefault);
        LOG.info("Initializing translator to " + locale + "...");
        translator.init(locale);
        preferences.setAvailableLanguages(translator.getAvailableLocales().values());
    }

}
