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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.resource.DefaultResourceResolver;
import org.springframework.context.MessageSource;

import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.resource.ResourceType.I18N;

public class MessageSourceTranslator implements Translator {

    private static final Logger LOG = LogManager.getLogger(MessageSourceTranslator.class);

    private final MessageSource messageSource;

    private final DefaultResourceResolver resourceResolver;

    private final String baseName;

    private Locale locale;

    private Map<String, Locale> localesMap;

    public MessageSourceTranslator(MessageSource messageSource,
                                   Preferences preferences,
                                   DefaultResourceResolver resourceResolver,
                                   String baseName) {
        this.messageSource = messageSource;
        this.resourceResolver = resourceResolver;
        this.baseName = baseName;
    }

    @Override
    public void init(Locale locale) {
        LOG.debug("Setting translation to locale " + locale);
        this.locale = locale;
    }

    @Override
    public String translate(String key, Object... parameters) {
        return messageSource.getMessage(key, parameters, locale);
    }

    @Override
    public Optional<Locale> getLocale() {
        return ofNullable(locale);
    }

    @Override
    public Map<String, Locale> getAvailableLocales() {
        if (localesMap == null) {
            String fileBaseName = ofNullable(Paths.get(baseName).getFileName())
                .map(Object::toString)
                .orElse(baseName)
                .concat("_");

            localesMap = resourceResolver.resolveResources(I18N, ".properties")
                .map(FilenameUtils::getBaseName)
                .filter(fileName -> fileName.startsWith(fileBaseName))
                .map(fileName -> fileName.substring(fileBaseName.length()))
                .map(this::toLocale)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Object::toString,
                                          Function.identity()));
        }

        return localesMap;
    }

    private Optional<Locale> toLocale(String filename) {
        try {
            return Optional.ofNullable(LocaleUtils.toLocale(filename));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
