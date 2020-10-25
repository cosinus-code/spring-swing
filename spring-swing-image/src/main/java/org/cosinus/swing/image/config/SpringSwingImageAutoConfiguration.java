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

package org.cosinus.swing.image.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cosinus.swing.boot.condition.ConditionalOnLinux;
import org.cosinus.swing.boot.condition.ConditionalOnMac;
import org.cosinus.swing.boot.condition.ConditionalOnWindows;
import org.cosinus.swing.context.ApplicationProperties;
import org.cosinus.swing.image.ImageHandler;
import org.cosinus.swing.image.icon.*;
import org.cosinus.swing.resource.ResourceResolver;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration related to images
 */
@Configuration
@ConditionalOnClass(ImageHandler.class)
public class SpringSwingImageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ImageHandler imageHandler() {
        return new ImageHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public IconsCache iconsCache() {
        return new DefaultIconsCache();
    }

    @Bean
    @ConditionalOnMissingBean
    public IconHandler iconHandler(ResourceResolver resourceResolver,
                                   IconsCache iconCache,
                                   IconProvider fileIcons,
                                   ApplicationUIHandler uiHandler,
                                   ImageHandler imageHandler) {
        return new IconHandler(resourceResolver,
                               iconCache,
                               fileIcons,
                               uiHandler,
                               imageHandler);
    }

    @Bean
    @ConditionalOnWindows
    public IconProvider windowsIconProvider() {
        return new WindowsIconProvider();
    }

    @Bean
    @ConditionalOnLinux
    public IconProvider linuxIconProvider(ApplicationProperties applicationProperties,
                                          IconsMapProvider iconsMapProvider) {
        return new LinuxIconProvider(applicationProperties, iconsMapProvider);
    }

    @Bean
    @ConditionalOnLinux
    public IconsMapProvider iconsMapProvider(ObjectMapper objectMapper,
                                             ResourceResolver resourceResolver) {
        return new IconsMapProvider(objectMapper, resourceResolver);
    }

    @Bean
    @ConditionalOnMac
    public IconProvider macIconProvider(ImageHandler imageHandler) {
        return new MacIconProvider(imageHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    public IconProvider iconProvider(ApplicationUIHandler uiHandler) {
        return new DefaultIconProvider(uiHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationImageInitializer applicationImageInitializer(IconProvider iconProvider) {
        return new ApplicationImageInitializer(iconProvider);
    }
}