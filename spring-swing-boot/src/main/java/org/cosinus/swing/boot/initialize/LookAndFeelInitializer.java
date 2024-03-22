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
import org.cosinus.swing.context.UIProperties;
import org.cosinus.swing.ui.ApplicationUIHandler;

import javax.swing.*;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

/**
 * Implementation of {@link ApplicationInitializer} for initializing the custom look-and-feel of the application.
 */
public class LookAndFeelInitializer implements ApplicationInitializer {

    private static final Logger LOG = LogManager.getLogger(LookAndFeelInitializer.class);

    private static final String CROSS_PLATFORM_UI_THEME = "cross-platform";

    private final ApplicationUIHandler uiHandler;

    private final UIProperties uiProperties;

    public LookAndFeelInitializer(ApplicationUIHandler uiHandler, UIProperties uiProperties) {
        this.uiHandler = uiHandler;
        this.uiProperties = uiProperties;
    }

    @Override
    public void initialize() {
        String theme = uiProperties.getTheme();
        if (!"default".equals(theme)) {
            String lookAndFeelClassName = ofNullable(theme)
                .filter(not(CROSS_PLATFORM_UI_THEME::equals))
                .flatMap(this::getLookAndFeelClassName)
                .orElseGet(uiHandler::getCrossPlatformLookAndFeelClassName);

            LOG.info("Initializing application look-and-feel to " + lookAndFeelClassName + "...");
            uiHandler.setLookAndFeel(lookAndFeelClassName);
        }
    }

    private Optional<String> getLookAndFeelClassName(String uiThemeName) {
        return uiHandler.getAvailableLookAndFeels()
            .entrySet()
            .stream()
            .filter(entry -> entry.getKey().equalsIgnoreCase(uiThemeName))
            .findFirst()
            .map(Map.Entry::getValue)
            .map(UIManager.LookAndFeelInfo::getClassName);
    }
}
