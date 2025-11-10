/*
 * Copyright 2025 Cosinus Software
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.cosinus.swing.boot.config;

import com.formdev.flatlaf.FlatLightLaf;
import org.cosinus.swing.boot.condition.ConditionalOnOperatingSystem;
import org.cosinus.swing.boot.initialize.LookAndFeelInitializer;
import org.cosinus.swing.context.UIProperties;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.resource.ClasspathResourceResolver;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.ui.dark.DarkLookAndFeel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import static org.cosinus.swing.os.OperatingSystem.MAC;
import static org.cosinus.swing.os.OperatingSystem.WINDOWS;

/**
 * Application UI Configuration.
 * <p>
 * This includes all UI related beans configuration.
 */
@AutoConfiguration
public class ApplicationUIAutoConfiguration {

    /**
     * LookAndFeel initializer using "swing.ui.theme" application property value.
     *
     * @param preferences the application preferences
     * @param uiHandler    the UI handler
     * @param uiProperties the UI properties
     * @return the {@link LookAndFeelInitializer} bean
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "swing.ui.theme", matchIfMissing = true)
    public LookAndFeelInitializer defaultThemeInitializer(
        final Preferences preferences,
        final ApplicationUIHandler uiHandler,
        final UIProperties uiProperties,
        final ClasspathResourceResolver resourceResolver,
        @Autowired(required = false) final DarkLookAndFeel darkLookAndFeel) {

        return new LookAndFeelInitializer(uiProperties, preferences, uiHandler, resourceResolver, darkLookAndFeel);
    }

    /**
     * The Darcula based implementation (FlatLight) of a dark look and feel bean
     *
     * @return the {@link DarkLookAndFeel} bean
     */
    @Bean
    @ConditionalOnClass(FlatLightLaf.class)
    @ConditionalOnOperatingSystem({WINDOWS, MAC})
    @ConditionalOnMissingBean
    public DarkLookAndFeel darkLookAndFeel() {
        return new DarkLookAndFeel();
    }

}
