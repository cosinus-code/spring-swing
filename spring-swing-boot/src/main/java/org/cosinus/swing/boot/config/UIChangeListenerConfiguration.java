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

import org.cosinus.swing.boot.condition.ConditionalOnKDE;
import org.cosinus.swing.boot.condition.ConditionalOnListeningUIChanges;
import org.cosinus.swing.boot.condition.ConditionalOnOperatingSystem;
import org.cosinus.swing.boot.initialize.LookAndFeelInitializer;
import org.cosinus.swing.boot.ui.DefaultUIChangeListener;
import org.cosinus.swing.ui.UIProperties;
import org.cosinus.swing.ui.listener.UIChangeController;
import org.cosinus.swing.ui.listener.UIThemeProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import static org.cosinus.swing.os.OperatingSystem.MAC;
import static org.cosinus.swing.os.OperatingSystem.WINDOWS;

@Configuration
@EnableScheduling
@ConditionalOnListeningUIChanges
public class UIChangeListenerConfiguration {

    @Bean
    public UIChangeController uiChangeController(final UIThemeProvider uiThemeProvider,
                                                 final UIProperties uiProperties) {
        return new UIChangeController(uiThemeProvider, uiProperties);
    }

    @Bean
    @ConditionalOnOperatingSystem({MAC, WINDOWS})
    public DefaultUIChangeListener defaultUIChangeListener(final UIChangeController uiChangeController,
                                                           final LookAndFeelInitializer lookAndFeelInitializer) {
        return new DefaultUIChangeListener(uiChangeController, lookAndFeelInitializer);
    }

    @Bean
    @ConditionalOnKDE
    public DefaultUIChangeListener kdeUIChangeListener(final UIChangeController uiChangeController,
                                                       final LookAndFeelInitializer lookAndFeelInitializer) {
        return new DefaultUIChangeListener(uiChangeController, lookAndFeelInitializer);
    }
}
