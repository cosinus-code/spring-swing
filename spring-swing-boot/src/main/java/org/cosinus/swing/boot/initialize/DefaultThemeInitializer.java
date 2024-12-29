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
import org.cosinus.swing.preference.Preference;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.resource.ClasspathResourceResolver;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.ui.dark.DarkLookAndFeel;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.preference.Preferences.LOOK_AND_FEEL;

/**
 * Implementation of {@link ApplicationInitializer} for initializing the default application theme.
 */
public class DefaultThemeInitializer implements ApplicationInitializer {

    private static final Logger LOG = LogManager.getLogger(DefaultThemeInitializer.class);

    private final Preferences preferences;

    private final ApplicationUIHandler uiHandler;

    private final ClasspathResourceResolver resourceResolver;

    private final DarkLookAndFeel darkLookAndFeel;

    public DefaultThemeInitializer(final Preferences preferences,
                                   final ApplicationUIHandler uiHandler,
                                   final ClasspathResourceResolver resourceResolver,
                                   final DarkLookAndFeel darkLookAndFeel) {
        this.preferences = preferences;
        this.uiHandler = uiHandler;
        this.resourceResolver = resourceResolver;
        this.darkLookAndFeel = darkLookAndFeel;
    }

    @Override
    public void initialize() {
        Map<String, LookAndFeelInfo> availableLookAndFeels = uiHandler.getAvailableLookAndFeels();
        preferences.findPreference(LOOK_AND_FEEL)
            .map(Preference::getValue)
            .map(Object::toString)
            .map(availableLookAndFeels::get)
            .map(LookAndFeelInfo::getClassName)
            .or(this::getDefaultLookAndFeelClassName)
            .ifPresent(lookAndFeelClassName -> {
                LOG.info("Initializing application look-and-feel to {}...", lookAndFeelClassName);
                uiHandler.setLookAndFeel(lookAndFeelClassName);
                customizeLookAndFeel(lookAndFeelClassName);

                uiHandler.translateDefaultUILabels();
                uiHandler.initializeDefaultUIFonts();

                preferences.setAvailableLookAndFeels(availableLookAndFeels.values());
            });

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
