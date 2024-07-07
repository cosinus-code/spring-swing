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
import org.cosinus.swing.boot.initialize.TranslatorInitializer;
import org.cosinus.swing.context.ApplicationContextInjector;
import org.cosinus.swing.context.ApplicationProperties;
import org.cosinus.swing.context.UIProperties;
import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.error.form.DefaultErrorFormProvider;
import org.cosinus.swing.exec.*;
import org.cosinus.swing.format.FormatHandler;
import org.cosinus.swing.menu.JsonMenuProvider;
import org.cosinus.swing.menu.MenuProvider;
import org.cosinus.swing.preference.JsonPreferencesProvider;
import org.cosinus.swing.preference.Preferences;
import org.cosinus.swing.preference.PreferencesProvider;
import org.cosinus.swing.preference.dialog.DefaultPreferencesDialogProvider;
import org.cosinus.swing.preference.dialog.PreferencesDialogProvider;
import org.cosinus.swing.resource.ClasspathResourceResolver;
import org.cosinus.swing.resource.DefaultResourceResolver;
import org.cosinus.swing.resource.FilesystemResourceResolver;
import org.cosinus.swing.resource.ResourceResolver;
import org.cosinus.swing.store.ApplicationStorage;
import org.cosinus.swing.store.LocalApplicationStorage;
import org.cosinus.swing.translate.MessageSourceTranslator;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.cosinus.swing.ui.UIDescriptorProvider;
import org.cosinus.swing.ui.UiInitializer;
import org.cosinus.swing.window.DefaultWindowSettingsHandler;
import org.cosinus.swing.window.WindowSettingsHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.swing.UIManager.LookAndFeelInfo;
import java.util.Set;

import static org.cosinus.swing.boot.SpringSwingApplication.applicationClass;
import static org.cosinus.swing.ui.UiInitializer.SWING_UI_INITIALIZER_PROPERTY;

/**
 * Application main configuration.
 * <p>
 * Here the main application beans are configured.
 */
@AutoConfiguration
@EnableConfigurationProperties({
    ApplicationProperties.class,
    UIProperties.class
})
public class SpringSwingAutoConfiguration {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
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
    public MenuProvider menuProvider(final ObjectMapper objectMapper,
                                     final Set<ResourceResolver> resourceResolvers) {
        return new JsonMenuProvider(objectMapper, resourceResolvers);
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
    @ConditionalOnMissingBean
    public ApplicationStorage applicationStorage() {
        return new LocalApplicationStorage(applicationClass);
    }

    @Bean
    public WindowSettingsHandler frameSettingsHandler(final ApplicationStorage localStorage) {
        return new DefaultWindowSettingsHandler(localStorage);
    }

    @Bean
    public DialogHandler dialogHandler(final Translator translator,
                                       final ApplicationUIHandler uiHandler,
                                       final PreferencesDialogProvider preferencesDialogProvider) {
        return new DialogHandler(translator,
            uiHandler,
            preferencesDialogProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultErrorFormProvider errorFormProvider() {
        return new DefaultErrorFormProvider();
    }

    @Bean
    @ConditionalOnMissingBean
    public PreferencesDialogProvider preferencesDialogProvider(final Translator translator) {
        return new DefaultPreferencesDialogProvider(translator);
    }

    @Bean
    @ConditionalOnMissingBean
    public ErrorHandler errorHandler(final Translator translator,
                                     final DefaultErrorFormProvider errorFormProvider) {
        return new ErrorHandler(translator,
            errorFormProvider);
    }

    @Bean
    public ApplicationUIHandler uiHandler(final Translator translator,
                                          final ProcessExecutor processExecutor,
                                          final Set<LookAndFeelInfo> lookAndFeels) {
        return new ApplicationUIHandler(translator, processExecutor, lookAndFeels);
    }

    @Bean
    public TranslatorInitializer translatorInitializer(final Preferences preferences,
                                                       final Translator translator) {
        return new TranslatorInitializer(preferences, translator);
    }

    @Bean
    public ApplicationInitializationHandler applicationInitializationHandler(
        final Set<ApplicationInitializer> applicationInitializers,
        final ApplicationFrameInitializer applicationFrameInitializer) {

        return new ApplicationInitializationHandler(applicationInitializers, applicationFrameInitializer);
    }

    @Bean
    public ApplicationFrameInitializer applicationContentInitializer(final ApplicationFrame applicationFrame) {
        return new ApplicationFrameInitializer(applicationFrame);
    }

    @Bean
    public QuitAction QuitAction() {
        return new QuitAction();
    }

    @Bean
    public KeyMapHandler keyMapHandler(final Set<ActionInContext> actions) {
        return new KeyMapHandler(actions);
    }

    @Bean
    public ActionController actionController(final ErrorHandler errorHandler,
                                             final KeyMapHandler keyMapHandler,
                                             final ActionContextProvider actionContextProvider,
                                             final Set<ActionInContext> actions) {
        return new ActionController(errorHandler,
            keyMapHandler,
            actionContextProvider,
            actions);
    }

    @Bean
    @ConditionalOnMissingBean
    public ActionContextProvider ActionContextProvider() {
        return new DefaultActionContextProvider();
    }

    @Bean
    public ActionExecutors actionExecutors(final Set<ActionExecutor<?>> executors) {
        return new ActionExecutors(executors);
    }

    @Bean
    public FormatHandler formatService() {
        return new FormatHandler();
    }

    @Bean
    public ApplicationContextInjector swingInjector(final ApplicationContext applicationContext) {
        return new ApplicationContextInjector(applicationContext);
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
    @ConditionalOnProperty(value = SWING_UI_INITIALIZER_PROPERTY, havingValue = "true")
    public UIDescriptorProvider uiDescriptorProvider(final ObjectMapper objectMapper,
                                                     final Set<ResourceResolver> resourceResolvers) {
        return new UIDescriptorProvider(objectMapper, resourceResolvers);
    }

    @Bean
    @ConditionalOnProperty(value = SWING_UI_INITIALIZER_PROPERTY, havingValue = "true")
    public UiInitializer uiInitializer(final UIDescriptorProvider uiDescriptorProvider,
                                       final Translator translator) {
        return new UiInitializer(uiDescriptorProvider, translator);
    }
}
