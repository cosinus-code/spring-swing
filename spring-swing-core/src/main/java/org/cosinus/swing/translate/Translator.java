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

package org.cosinus.swing.translate;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public interface Translator {

    /**
     * Initialize the translator
     *
     * @param locale the locale to initialize the translator
     */
    void init(Locale locale);

    /**
     * Translate a key with parameters
     *
     * @param key        the key to translate
     * @param parameters the parameters for translation
     * @return the translation
     */
    String translate(String key, Object... parameters);

    Optional<Locale> getLocale();

    Map<String, Locale> getAvailableLocales();
}
