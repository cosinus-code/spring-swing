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

package org.cosinus.swing.image.config;

import org.apache.commons.imaging.formats.icns.IcnsImageParser;
import org.cosinus.swing.boot.condition.*;
import org.cosinus.swing.context.ApplicationProperties;
import org.cosinus.swing.file.FileHandler;
import org.cosinus.swing.file.FileSystem;
import org.cosinus.swing.file.mimetype.MimeTypeResolver;
import org.cosinus.swing.image.ImageHandler;
import org.cosinus.swing.image.icon.*;
import org.cosinus.swing.resource.ClasspathResourceResolver;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.ui.listener.UIChangeController;
import org.cosinus.swing.ui.listener.UIThemeProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;

/**
 * Configuration related to images
 */
@AutoConfiguration
@ConditionalOnClass(ImageHandler.class)
public class SpringSwingImageAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ImageHandler imageHandler(final FileHandler fileHandler) {
        return new ImageHandler(fileHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    public IconHandler iconHandler(final ClasspathResourceResolver resourceResolver,
                                   final IconProvider fileIcons,
                                   final ApplicationUIHandler uiHandler,
                                   final ImageHandler imageHandler) {
        return new IconHandler(resourceResolver,
            fileIcons,
            uiHandler,
            imageHandler);
    }

    @Bean
    @ConditionalOnWindows
    public IconNameProvider windowsIconNameProvider() {
        return new WindowsIconNameProvider();
    }

    @Bean
    @ConditionalOnWindows
    public IconProvider windowsIconProvider(final ImageHandler imageHandler,
                                            final IconNameProvider iconNameProvider) {
        return new WindowsIconProvider(imageHandler, iconNameProvider);
    }

    @Bean
    @ConditionalOnKDE
    public KdeIconNameProvider kdeIconNameProvider() {
        return new KdeIconNameProvider();
    }

    @Bean
    @ConditionalOnGnome
    public GnomeIconNameProvider gnomeIconNameProvider() {
        return new GnomeIconNameProvider();
    }

    @Bean
    @ConditionalOnLinux
    public IconProvider linuxIconProvider(final ApplicationProperties applicationProperties,
                                          final ApplicationUIHandler uiHandler,
                                          final UIThemeProvider uiThemeProvider,
                                          final MimeTypeResolver mimeTypeResolver,
                                          final IconNameProvider iconNameProvider) {
        return new LinuxIconProvider(
            applicationProperties, uiHandler, uiThemeProvider, mimeTypeResolver, iconNameProvider);
    }

    @Bean
    @ConditionalOnMac
    public IcnsImageParser icnsImageParser() {
        return new IcnsImageParser();
    }

    @Bean
    @ConditionalOnMac
    public IconNameProvider macIconNameProvider() {
        return new MacIconNameProvider();
    }

    @Bean
    @ConditionalOnMac
    public IconProvider macIconProvider(final FileSystem fileSystem,
                                        final IcnsImageParser icnsImageParser,
                                        final ImageHandler imageHandler,
                                        final IconNameProvider iconNameProvider) {
        return new MacIconProvider(fileSystem, icnsImageParser, imageHandler, iconNameProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public IconProvider iconProvider(ApplicationUIHandler uiHandler) {
        return new DefaultIconProvider(uiHandler);
    }

    @Bean
    @ConditionalOnMissingBean
    public IconNameProvider defaultIconNameProvider() {
        return new DefaultIconNameProvider();
    }


    @Bean
    @ConditionalOnMissingBean
    public ApplicationImageInitializer applicationImageInitializer(final IconInitializer iconInitializer,
                                                                   @Autowired(required = false)
                                                                   final UIChangeController uiChangeController) {
        return new ApplicationImageInitializer(iconInitializer, uiChangeController);
    }

    @Bean("fileExtensionKeyGenerator")
    public KeyGenerator keyGenerator() {
        return new FileExtensionKeyGenerator();
    }

    @Bean
    public IconInitializer iconInitializer(final IconHandler iconHandler) {
        return new IconInitializer(iconHandler);
    }
}
