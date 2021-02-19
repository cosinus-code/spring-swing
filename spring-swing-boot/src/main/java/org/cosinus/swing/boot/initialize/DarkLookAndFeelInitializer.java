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

import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.resource.ResourceResolver;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.ui.dark.DarkLookAndFeel;

import javax.swing.*;

/**
 * Application initializer for dark look-and-feel
 */
public class DarkLookAndFeelInitializer implements ApplicationInitializer {

    private final Preferences preferences;

    private final ApplicationUIHandler uiHandler;

    private final DarkLookAndFeel darkLookAndFeel;

    private final ResourceResolver resourceResolver;

    public DarkLookAndFeelInitializer(Preferences preferences,
                                      ApplicationUIHandler uiHandler,
                                      DarkLookAndFeel darkLookAndFeel, ResourceResolver resourceResolver) {
        this.preferences = preferences;
        this.uiHandler = uiHandler;
        this.darkLookAndFeel = darkLookAndFeel;
        this.resourceResolver = resourceResolver;
    }

    @Override
    public void initialize() {
        if (uiHandler.isDarkTheme() && noLookAndFeelPreference()) {
            uiHandler.setLookAndFeel(darkLookAndFeel.getClassName());
            if (uiHandler.getDefaultIcon().isEmpty()) {
                resourceResolver.resolveImageAsBytes("dark/file.png")
                        .map(ImageIcon::new)
                        .ifPresent(uiHandler::setDefaultIcon);
            }
        }
    }

    private boolean noLookAndFeelPreference() {
        return preferences.getStringPreference(Preferences.LOOK_AND_FEEL)
                .filter(lookAndFeelName -> !lookAndFeelName.isEmpty())
                .isEmpty();
    }
}
