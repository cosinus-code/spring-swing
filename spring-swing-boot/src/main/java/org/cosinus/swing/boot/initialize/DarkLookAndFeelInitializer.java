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

import static org.cosinus.swing.preference.Preferences.LOOK_AND_FEEL;

/**
 * Application initializer for dark look-and-feel
 */
public class DarkLookAndFeelInitializer implements ApplicationInitializer {

    private static final Logger LOG = LogManager.getLogger(DarkLookAndFeelInitializer.class);

    private final Preferences preferences;

    private final ApplicationUIHandler uiHandler;

    private final DarkLookAndFeel darkLookAndFeel;

    private final ClasspathResourceResolver resourceResolver;

    public DarkLookAndFeelInitializer(Preferences preferences,
                                      ApplicationUIHandler uiHandler,
                                      DarkLookAndFeel darkLookAndFeel,
                                      ClasspathResourceResolver resourceResolver) {
        this.preferences = preferences;
        this.uiHandler = uiHandler;
        this.darkLookAndFeel = darkLookAndFeel;
        this.resourceResolver = resourceResolver;
    }

    @Override
    public void initialize() {
        if (uiHandler.isDarkTheme() && noLookAndFeelPreference()) {
            LOG.info("Initializing application dark theme...");
            uiHandler.setLookAndFeel(darkLookAndFeel.getClassName());
            if (uiHandler.getDefaultIcon().isEmpty()) {
                resourceResolver.resolveImageAsBytes("dark/file.png")
                    .map(ImageIcon::new)
                    .ifPresent(uiHandler::setDefaultIcon);
            }
        }
    }

    private boolean noLookAndFeelPreference() {
        return preferences.findPreference(LOOK_AND_FEEL)
            .map(Preference::getValue)
            .map(Object::toString)
            .filter(lookAndFeelName -> !lookAndFeelName.isEmpty())
            .isEmpty();
    }
}
