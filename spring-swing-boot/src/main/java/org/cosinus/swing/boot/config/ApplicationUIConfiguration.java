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
import org.cosinus.swing.boot.initialize.ApplicationUIInitializer;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.resource.ResourceResolver;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.boot.initialize.CrossPlatformLookAndFeelInitializer;
import org.cosinus.swing.ui.dark.DarkLookAndFeel;
import org.cosinus.swing.boot.initialize.DarkLookAndFeelInitializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application UI Configuration
 */
@Configuration
public class ApplicationUIConfiguration {

    @Bean
    @ConditionalOnProperty(value = "swing.ui.theme", havingValue = "java")
    public CrossPlatformLookAndFeelInitializer crossPlatformLookAndFeelInitializer(ApplicationUIHandler uiHandler) {
        return new CrossPlatformLookAndFeelInitializer(uiHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(value = "swing.ui.theme", havingValue = "custom", matchIfMissing = true)
    public ApplicationUIInitializer applicationUIInitializer(Preferences preferences, ApplicationUIHandler uiHandler) {
        return new ApplicationUIInitializer(preferences, uiHandler);
    }

    @Bean
    @ConditionalOnClass(DarculaLaf.class)
    @ConditionalOnProperty(value = "swing.ui.theme", havingValue = "custom", matchIfMissing = true)
    public DarkLookAndFeelInitializer darkLookAndFeelInitializer(Preferences preferences,
                                                                 ApplicationUIHandler uiHandler,
                                                                 DarkLookAndFeel darkLookAndFeel,
                                                                 ResourceResolver resourceResolver) {
        return new DarkLookAndFeelInitializer(preferences,
                                              uiHandler,
                                              darkLookAndFeel,
                                              resourceResolver);
    }

    @Bean
    @ConditionalOnClass(DarculaLaf.class)
    public DarkLookAndFeel darkLookAndFeel() {
        return new DarkLookAndFeel();
    }

}