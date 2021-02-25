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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.cosinus.swing.action.*;
import org.cosinus.swing.action.execute.ActionExecutor;
import org.cosinus.swing.action.execute.ActionExecutors;
import org.cosinus.swing.boot.ApplicationFrame;
import org.cosinus.swing.boot.ApplicationInitializationHandler;
import org.cosinus.swing.boot.condition.ConditionalOnLinux;
import org.cosinus.swing.boot.condition.ConditionalOnMac;
import org.cosinus.swing.boot.condition.ConditionalOnWindows;
import org.cosinus.swing.boot.initialize.ApplicationFrameInitializer;
import org.cosinus.swing.boot.initialize.ApplicationInitializer;
import org.cosinus.swing.context.ApplicationProperties;
import org.cosinus.swing.context.SwingApplicationContext;
import org.cosinus.swing.context.SwingInjector;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.exec.*;
import org.cosinus.swing.form.DefaultWindowSettingsHandler;
import org.cosinus.swing.form.WindowSettingsHandler;
import org.cosinus.swing.form.error.ErrorFormProvider;
import org.cosinus.swing.form.menu.JsonMenuProvider;
import org.cosinus.swing.form.menu.MenuProvider;
import org.cosinus.swing.preference.JsonPreferencesProvider;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.preference.PreferencesProvider;
import org.cosinus.swing.resource.ResourceResolver;
import org.cosinus.swing.store.ApplicationStorage;
import org.cosinus.swing.store.LocalApplicationStorage;
import org.cosinus.swing.translate.MessageSourceTranslator;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.util.Set;

import static org.cosinus.swing.boot.SpringSwingApplication.applicationClass;

/**
 * Application configuration
 */
@Configuration
public class ApplicationConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean
    public ResourceResolver resourceResolver(ResourcePatternResolver resourceLoader,
                                             ApplicationProperties applicationProperties) {
        return new ResourceResolver(resourceLoader,
                                    applicationProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public MenuProvider menuProvider(ObjectMapper objectMapper,
                                     ResourceResolver resourceResolver) {
        return new JsonMenuProvider(objectMapper, resourceResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public PreferencesProvider preferencesProvider(ObjectMapper objectMapper,
                                                   ResourceResolver resourceResolver) {
        return new JsonPreferencesProvider(objectMapper,
                                           resourceResolver);
    }

    @Bean
    @ConditionalOnMissingBean
    public Preferences preferences(PreferencesProvider preferencesProvider) {
        return preferencesProvider.getPreferences()
            .orElseGet(Preferences::new);
    }

    @Bean
    @ConditionalOnMissingBean
    public ApplicationStorage localApplicationStorage() {
        return new LocalApplicationStorage(applicationClass);
    }

    @Bean
    public WindowSettingsHandler frameSettingsHandler(ApplicationStorage localStorage) {
        return new DefaultWindowSettingsHandler(localStorage);
    }

    @Bean
    public DialogHandler dialogHandler(SwingApplicationContext swingContext,
                                       Translator translator,
                                       ApplicationUIHandler uiHandler,
                                       ApplicationStorage localStorage) {
        return new DialogHandler(swingContext,
                                 translator,
                                 uiHandler,
                                 localStorage);
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorFormProvider errorFormProvider(SwingApplicationContext swingContext) {
        return new ErrorFormProvider(swingContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorHandler errorHandler(Translator translator,
                                     ErrorFormProvider errorFormProvider) {
        return new ErrorHandler(translator,
                                errorFormProvider);
    }

    @Bean
    public ApplicationUIHandler uiHandler(Translator translator,
                                          ProcessExecutor processExecutor) {
        return new ApplicationUIHandler(translator, processExecutor);
    }

    @Bean
    public ApplicationInitializationHandler applicationInitializationHandler(
        Set<ApplicationInitializer> applicationInitializers) {
        return new ApplicationInitializationHandler(applicationInitializers);
    }

    @Bean
    public ApplicationFrameInitializer applicationContentInitializer(ApplicationFrame applicationFrame) {
        return new ApplicationFrameInitializer(applicationFrame);
    }

    @Bean
    public QuitAction QuitAction() {
        return new QuitAction();
    }

    @Bean
    public <C extends ActionContext> KeyMapHandler<C> keyMapHandler(Set<ActionInContext<C>> actions) {
        return new KeyMapHandler<>(actions);
    }

    @Bean
    public <C extends ActionContext> ActionController<C> swingActionController(
        ErrorHandler errorHandler,
        KeyMapHandler<C> keyMapHandler,
        ActionContextProvider<C> actionContextProvider,
        Set<ActionInContext<C>> actions) {
        return new ActionController<>(errorHandler,
                                      keyMapHandler,
                                      actionContextProvider,
                                      actions);
    }

    @Bean
    @ConditionalOnMissingBean
    public ActionContextProvider<ActionContext> ActionContextProvider() {
        return new DefaultActionContextProvider();
    }

    @Bean
    public ActionExecutors actionExecutors(Set<ActionExecutor<?>> executors) {
        return new ActionExecutors(executors);
    }

    @Bean
    public SwingApplicationContext swingApplicationContext(ApplicationProperties applicationProperties) {
        return new SwingApplicationContext(applicationProperties);
    }

    @Bean
    public SwingInjector swingInjector(SwingApplicationContext swingContext) {
        return new SwingInjector(swingContext);
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
    public Translator translator(MessageSource messageSource,
                                 Preferences preferences,
                                 ResourceResolver resourceResolver,
                                 @Value("${swing.messages.basename:i18n/messages}") String baseName) {
        return new MessageSourceTranslator(messageSource, preferences, resourceResolver, baseName);
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
}
