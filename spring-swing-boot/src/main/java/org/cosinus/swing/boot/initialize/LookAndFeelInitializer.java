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

import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.context.UIProperties;
import org.cosinus.swing.preference.Preference;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.resource.ClasspathResourceResolver;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.ui.dark.DarkLookAndFeel;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;
import static org.cosinus.swing.preference.Preferences.LOOK_AND_FEEL;

/**
 * Implementation of {@link ApplicationInitializer} for initializing the application theme.
 */
public class LookAndFeelInitializer implements ApplicationInitializer {

    private static final Logger LOG = LogManager.getLogger(LookAndFeelInitializer.class);

    private final UIProperties uiProperties;

    private final Preferences preferences;

    private final ApplicationUIHandler uiHandler;

    private final ClasspathResourceResolver resourceResolver;

    private final DarkLookAndFeel darkLookAndFeel;

    public LookAndFeelInitializer(final UIProperties uiProperties,
                                  final Preferences preferences,
                                  final ApplicationUIHandler uiHandler,
                                  final ClasspathResourceResolver resourceResolver,
                                  final DarkLookAndFeel darkLookAndFeel) {
        this.uiProperties = uiProperties;
        this.preferences = preferences;
        this.uiHandler = uiHandler;
        this.resourceResolver = resourceResolver;
        this.darkLookAndFeel = darkLookAndFeel;
    }

    @Override
    public void initialize() {
        Map<String, LookAndFeelInfo> availableLookAndFeels = uiHandler.getAvailableLookAndFeels();

        ofNullable(uiProperties.getTheme())
            .filter(not("default"::equals))
            .map(theme -> availableLookAndFeels.entrySet()
                .stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(theme))
                .findFirst()
                .map(Entry::getValue)
                .map(UIManager.LookAndFeelInfo::getClassName)
                .orElseGet(uiHandler::getCrossPlatformLookAndFeelClassName))
            .or(() -> preferences.findPreference(LOOK_AND_FEEL)
                .map(Preference::getValue)
                .map(Object::toString)
                .map(availableLookAndFeels::get)
                .map(LookAndFeelInfo::getClassName)
                .or(this::getDefaultLookAndFeelClassName))
            .ifPresent(this::setLookAndFeel);
        preferences.setAvailableLookAndFeels(availableLookAndFeels.values());
    }

    private void setLookAndFeel(String lookAndFeelClassName) {
        LOG.info("Initializing application look-and-feel to {}...", lookAndFeelClassName);
        uiHandler.setLookAndFeel(lookAndFeelClassName);
        customizeLookAndFeel(lookAndFeelClassName);

        uiHandler.translateDefaultUILabels();
        uiHandler.initializeDefaultUIFonts();
    }

    private Optional<String> getDefaultLookAndFeelClassName() {
        return !uiHandler.isDarkTheme() ?
            Optional.of(uiHandler.getDefaultLookAndFeelClassName()) :
            ofNullable(darkLookAndFeel)
                .map(DarkLookAndFeel::getClassName);
    }

    private void customizeLookAndFeel(String lookAndFeelClassName) {
        ofNullable(darkLookAndFeel)
            .map(DarkLookAndFeel::getName)
            .filter(lookAndFeelClassName::equals)
            .ifPresent(lookAndFeelName -> {
                if (uiHandler.getDefaultFileIcon().isEmpty()) {
                    resourceResolver.resolveImageAsBytes("dark/file.png")
                        .map(ImageIcon::new)
                        .ifPresent(uiHandler::setDefaultFileIcon);
                }
            });
    }
}
