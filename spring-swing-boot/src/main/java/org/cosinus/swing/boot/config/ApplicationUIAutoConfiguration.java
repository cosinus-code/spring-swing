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

package org.cosinus.swing.boot.config;

import com.bulenkov.darcula.DarculaLaf;
import org.cosinus.swing.boot.condition.ConditionalOnOperatingSystem;
import org.cosinus.swing.boot.initialize.DarkLookAndFeelInitializer;
import org.cosinus.swing.boot.initialize.DefaultThemeInitializer;
import org.cosinus.swing.boot.initialize.LookAndFeelInitializer;
import org.cosinus.swing.context.UIProperties;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.resource.ClasspathResourceResolver;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.ui.dark.DarkLookAndFeel;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

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
     * @param uiHandler the UI handler
     * @param uiProperties the UI properties
     * @return the {@link LookAndFeelInitializer} bean
     */
    @Bean
    @ConditionalOnProperty(value = "swing.ui.theme")
    public LookAndFeelInitializer lookAndFeelInitializer(ApplicationUIHandler uiHandler,
                                                         UIProperties uiProperties) {
        return new LookAndFeelInitializer(uiHandler, uiProperties);
    }

    /**
     * LookAndFeel initializer for "swing.ui.theme=default" application property.
     *
     * @param preferences the application preferences
     * @param uiHandler the UI handler
     * @return the {@link DefaultThemeInitializer} bean
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "swing.ui.theme", havingValue = "default", matchIfMissing = true)
    public DefaultThemeInitializer defaultThemeInitializer(Preferences preferences,
                                                           ApplicationUIHandler uiHandler) {
        return new DefaultThemeInitializer(preferences, uiHandler);
    }

    /**
     * Dark LookAndFeel initializer for "swing.ui.theme=default" application property.
     *
     * @param preferences the application preferences
     * @param uiHandler the UI handler
     * @param darkLookAndFeel the dark look and feel
     * @param resourceResolver the resource resolver
     * @return the {@link DarkLookAndFeelInitializer} bean
     */
    @Bean
    @ConditionalOnBean(DarkLookAndFeel.class)
    @ConditionalOnProperty(value = "swing.ui.theme", havingValue = "default", matchIfMissing = true)
    public DarkLookAndFeelInitializer darkLookAndFeelInitializer(Preferences preferences,
                                                                 ApplicationUIHandler uiHandler,
                                                                 DarkLookAndFeel darkLookAndFeel,
                                                                 ClasspathResourceResolver resourceResolver) {
        return new DarkLookAndFeelInitializer(preferences,
                uiHandler,
                darkLookAndFeel,
                resourceResolver);
    }

    /**
     * The Darcula based implementation of a dark look and feel bean
     *
     * @return the {@link DarkLookAndFeel} bean
     */
    @Bean
    @ConditionalOnClass(DarculaLaf.class)
    @ConditionalOnOperatingSystem({"Windows", "Linux"})
    @ConditionalOnMissingBean
    public DarkLookAndFeel darkLookAndFeel() {
        return new DarkLookAndFeel();
    }

}
