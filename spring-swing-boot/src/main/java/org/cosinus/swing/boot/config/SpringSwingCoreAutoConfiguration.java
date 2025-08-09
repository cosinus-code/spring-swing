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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.cosinus.swing.boot.condition.ConditionalOnLinux;
import org.cosinus.swing.boot.condition.ConditionalOnMac;
import org.cosinus.swing.boot.condition.ConditionalOnWindows;
import org.cosinus.swing.context.ApplicationProperties;
import org.cosinus.swing.context.UIProperties;
import org.cosinus.swing.exec.*;
import org.cosinus.swing.preference.JsonPreferencesProvider;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.preference.PreferencesProvider;
import org.cosinus.swing.resource.ClasspathResourceResolver;
import org.cosinus.swing.resource.DefaultResourceResolver;
import org.cosinus.swing.resource.FilesystemResourceResolver;
import org.cosinus.swing.resource.ResourceResolver;
import org.cosinus.swing.translate.MessageSourceTranslator;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.swing.*;
import java.util.Set;

import static com.fasterxml.jackson.databind.DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE;
import static com.fasterxml.jackson.databind.MapperFeature.REQUIRE_HANDLERS_FOR_JAVA8_TIMES;
import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * Application core configuration.
 * <p>
 * Here the core application beans are configured.
 */
@AutoConfiguration
@EnableConfigurationProperties({
    ApplicationProperties.class,
    UIProperties.class
})
@Role(ROLE_INFRASTRUCTURE)
public class SpringSwingCoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper() {
        return JsonMapper
            .builder()
            .disable(REQUIRE_HANDLERS_FOR_JAVA8_TIMES)
            .disable(ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
            .disable(WRITE_DATES_AS_TIMESTAMPS)
            .build()
            .registerModule(new JavaTimeModule());
    }

    @Bean
    public ApplicationProperties applicationProperties() {
        return new ApplicationProperties();
    }

    @Bean
    public FilesystemResourceResolver filesystemResourceResolver(final ApplicationProperties applicationProperties) {
        return new FilesystemResourceResolver(applicationProperties);
    }

    @Bean
    public ClasspathResourceResolver classpathResourceResolver(final ResourcePatternResolver resourceLoader) {
        return new ClasspathResourceResolver(resourceLoader);
    }

    @Bean
    public DefaultResourceResolver defaultResourceResolver(final FilesystemResourceResolver filesystemResourceResolver,
                                                           final ClasspathResourceResolver classpathResourceResolver) {
        return new DefaultResourceResolver(filesystemResourceResolver,
            classpathResourceResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public PreferencesProvider preferencesProvider(final ObjectMapper objectMapper,
                                                   final Set<ResourceResolver> resourceResolvers) {
        return new JsonPreferencesProvider(objectMapper,
            resourceResolvers);
    }

    @Bean
    @ConditionalOnMissingBean
    public Preferences preferences(final PreferencesProvider preferencesProvider) {
        return preferencesProvider
            .getPreferences()
            .orElseGet(Preferences::new);
    }

    @Bean
    @ConditionalOnWindows
    public ProcessExecutor windowsProcessExecutor() {
        return new WindowsProcessExecutor();
    }

    @Bean
    @ConditionalOnLinux
    public ProcessExecutor linuxProcessExecutor() {
        return new LinuxProcessExecutor();
    }

    @Bean
    @ConditionalOnMac
    public ProcessExecutor macProcessExecutor() {
        return new MacProcessExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    public ProcessExecutor processExecutor() {
        return new DefaultProcessExecutor();
    }

    @Bean
    @ConditionalOnMissingBean
    public Translator translator(final MessageSource messageSource,
                                 final DefaultResourceResolver resourceResolver,
                                 @Value("${swing.messages.basename:i18n/messages}") String baseName) {
        return new MessageSourceTranslator(messageSource, resourceResolver, baseName);
    }

    @Bean
    @ConditionalOnMissingBean
    public MessageSource messageSource(@Value("${swing.messages.basename:i18n/messages}") String baseName) {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("file:" + baseName, baseName, "i18n/swing");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    @Bean
    public ApplicationUIHandler uiHandler(final Translator translator,
                                          final ProcessExecutor processExecutor,
                                          final Set<UIManager.LookAndFeelInfo> lookAndFeels) {
        return new ApplicationUIHandler(translator, processExecutor, lookAndFeels);
    }
}
