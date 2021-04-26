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
import org.cosinus.swing.ui.ApplicationUIHandler;

import javax.swing.UIManager.LookAndFeelInfo;
import java.util.Map;

import static org.cosinus.swing.preference.Preferences.LOOK_AND_FEEL;

/**
 * Swing UI initializer
 */
public class DefaultThemeInitializer implements ApplicationInitializer {

    private static final Logger LOG = LogManager.getLogger(DefaultThemeInitializer.class);

    private final Preferences preferences;

    private final ApplicationUIHandler uiHandler;

    public DefaultThemeInitializer(Preferences preferences,
                                   ApplicationUIHandler uiHandler) {
        this.preferences = preferences;
        this.uiHandler = uiHandler;
    }

    @Override
    public void initialize() {
        LOG.info("Initializing application default theme...");
        Map<String, LookAndFeelInfo> availableLookAndFeels = uiHandler.getAvailableLookAndFeels();
        String lookAndFeelClassName = preferences.findPreference(LOOK_AND_FEEL)
            .map(Preference::getValue)
            .map(Object::toString)
            .map(availableLookAndFeels::get)
            .map(LookAndFeelInfo::getClassName)
            .orElseGet(uiHandler::getDefaultLookAndFeelClassName);
        uiHandler.setLookAndFeel(lookAndFeelClassName);
        uiHandler.translateDefaultUILabels();
        uiHandler.initializeDefaultUIFonts();

        preferences.setAvailableLookAndFeels(availableLookAndFeels.values());
    }
}
