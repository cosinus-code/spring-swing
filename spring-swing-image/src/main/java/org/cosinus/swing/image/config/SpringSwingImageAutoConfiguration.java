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

import org.apache.commons.imaging.formats.icns.IcnsImageParser;
import org.cosinus.swing.boot.condition.ConditionalOnLinux;
import org.cosinus.swing.boot.condition.ConditionalOnMac;
import org.cosinus.swing.boot.condition.ConditionalOnWindows;
import org.cosinus.swing.context.ApplicationProperties;
import org.cosinus.swing.exec.ProcessExecutor;
import org.cosinus.swing.image.ImageHandler;
import org.cosinus.swing.image.icon.DefaultIconProvider;
import org.cosinus.swing.image.icon.FileExtensionKeyGenerator;
import org.cosinus.swing.image.icon.IconHandler;
import org.cosinus.swing.image.icon.IconProvider;
import org.cosinus.swing.image.icon.LinuxIconProvider;
import org.cosinus.swing.image.icon.MacIconProvider;
import org.cosinus.swing.image.icon.WindowsIconProvider;
import org.cosinus.swing.io.MimeTypeResolver;
import org.cosinus.swing.resource.ClasspathResourceResolver;
import org.cosinus.swing.ui.ApplicationUIHandler;
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
    public ImageHandler imageHandler() {
        return new ImageHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public IconHandler iconHandler(ClasspathResourceResolver resourceResolver,
                                   IconProvider fileIcons,
                                   ApplicationUIHandler uiHandler,
                                   ImageHandler imageHandler) {
        return new IconHandler(resourceResolver,
            fileIcons,
            uiHandler,
            imageHandler);
    }

    @Bean
    @ConditionalOnWindows
    public IconProvider windowsIconProvider(ImageHandler imageHandler) {
        return new WindowsIconProvider(imageHandler);
    }

    @Bean
    @ConditionalOnLinux
    public IconProvider linuxIconProvider(final ApplicationProperties applicationProperties,
                                          final ApplicationUIHandler uiHandler,
                                          final ProcessExecutor processExecutor,
                                          final MimeTypeResolver mimeTypeResolver) {
        return new LinuxIconProvider(applicationProperties, uiHandler, processExecutor, mimeTypeResolver);
    }

    @Bean
    @ConditionalOnMac
    public IcnsImageParser icnsImageParser() {
        return new IcnsImageParser();
    }

    @Bean
    @ConditionalOnMac
    public IconProvider macIconProvider(IcnsImageParser icnsImageParser, ImageHandler imageHandler) {
        return new MacIconProvider(icnsImageParser, imageHandler);
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

    @Bean("fileExtensionKeyGenerator")
    public KeyGenerator keyGenerator(final IconProvider iconProvider) {
        return new FileExtensionKeyGenerator(iconProvider);
    }
}
