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

package org.cosinus.swing.boot.splash;

import org.cosinus.swing.boot.ApplicationFrame;
import org.cosinus.swing.boot.SpringSwingApplication;
import org.cosinus.swing.boot.SwingSpringApplicationStartupListener;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

/**
 * Implementation of {@link SwingSpringApplicationStartupListener}
 * which updates the splash with the application startup progress.
 * <p>
 * The splash progress bar is controlled by dedicated application arguments.
 * The bean context initialization is covered between the bounds established by
 * CONTEXT_INITIALIZATION_MIN_PERCENT and CONTEXT_INITIALIZATION_MAX_PERCENT constants;
 */
public class SplashApplicationStartupListener implements SwingSpringApplicationStartupListener {

    private static final String LOG_STARTUP_PROGRESS = "-log-startup-progress";
    private static final String SPLASH_PROGRESS = "-splash-progress";
    private static final String SPLASH_PROGRESS_COLOR = "-splash-progress-color";
    private static final String SPLASH_PROGRESS_X = "-splash-progress-x";
    private static final String SPLASH_PROGRESS_Y = "-splash-progress-y";
    private static final String SPLASH_PROGRESS_WIDTH = "-splash-progress-width";
    private static final String SPLASH_PROGRESS_HEIGHT = "-splash-progress-height";

    private static final String APPLICATION_STATUS_STARTING = "Starting application...";
    private static final String APPLICATION_STATUS_PREPARED = "Application prepared";
    private static final String APPLICATION_STATUS_CONTEXT_INITIALING = "Initializing application context...";
    private static final String APPLICATION_STATUS_STARTED = "Application started";
    private static final String APPLICATION_STATUS_INITIALIZING_FRAME = "Initializing application frame...";
    private static final String APPLICATION_STATUS_BEAN_INITIALIZED = "Context bean initialized: ";

    private static final int CONTEXT_INITIALIZATION_MIN_PERCENT = 5;
    private static final int CONTEXT_INITIALIZATION_MAX_PERCENT = 99;

    private final ApplicationSplash splash;

    private final SpringSwingApplication application;

    private int totalBeansCount;

    private Set<String> beanNames = new HashSet<>();

    public SplashApplicationStartupListener(SpringApplication application, String[] arguments) {
        Map<String, String> argumentsMap = ofNullable(arguments)
            .stream()
            .flatMap(Arrays::stream)
            .map(argument -> argument.split("="))
            .collect(toMap(
                argument -> argument[0],
                argument -> argument.length > 1 ? argument[1] : "",
                (k1, k2) -> k2,
                HashMap::new));

        boolean logStartupProgress = argumentsMap.containsKey(LOG_STARTUP_PROGRESS);
        boolean splashProgress = argumentsMap.containsKey(SPLASH_PROGRESS);
        String splashProgressColor = argumentsMap.get(SPLASH_PROGRESS_COLOR);
        String splashProgressX = argumentsMap.get(SPLASH_PROGRESS_X);
        String splashProgressY = argumentsMap.get(SPLASH_PROGRESS_Y);
        String splashProgressWidth = argumentsMap.get(SPLASH_PROGRESS_WIDTH);
        String splashProgressHeight = argumentsMap.get(SPLASH_PROGRESS_HEIGHT);

        this.splash = new ApplicationSplash(splashProgress, splashProgressColor, splashProgressX, splashProgressY,
            splashProgressWidth, splashProgressHeight);
        this.application = (SpringSwingApplication) application;

        this.application.setLogStartupProgress(logStartupProgress);
        this.application.logStartupProgress(1, splash::toString);
    }

    @Override
    public void starting(ConfigurableBootstrapContext bootstrapContext) {
        updateSplash(APPLICATION_STATUS_STARTING,
            CONTEXT_INITIALIZATION_MIN_PERCENT / 4);
    }

    @Override
    public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext,
                                    ConfigurableEnvironment environment) {
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {
        updateSplash(APPLICATION_STATUS_PREPARED,
            3 * CONTEXT_INITIALIZATION_MIN_PERCENT / 4);
        totalBeansCount = context.getBeanDefinitionCount();
    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {
        totalBeansCount = context.getBeanDefinitionCount();
    }

    @Override
    public void contextBeforeInitializeBeans(ApplicationContext context) {
        updateSplash(APPLICATION_STATUS_CONTEXT_INITIALING,
            CONTEXT_INITIALIZATION_MIN_PERCENT);

        beanNames = stream(context.getBeanDefinitionNames())
            .collect(Collectors.toSet());

        stream(context.getBeanNamesForType(BeanPostProcessor.class))
            .forEach(beanNames::remove);

        stream(context.getBeanNamesForType(BeanFactoryPostProcessor.class))
            .forEach(beanNames::remove);

        totalBeansCount = beanNames.size();
    }

    @Override
    public void contextBeforeInitializeBean(ApplicationContext context,
                                            Object bean, String beanName) {
    }

    @Override
    public void contextAfterInitializeBean(ApplicationContext context,
                                           Object bean, String beanName) {
        beanNames.remove(beanName);
        int percent = (int) ((CONTEXT_INITIALIZATION_MAX_PERCENT - CONTEXT_INITIALIZATION_MIN_PERCENT) *
            (1 - (double) beanNames.size() / totalBeansCount));
        updateSplash(APPLICATION_STATUS_BEAN_INITIALIZED + beanName,
            CONTEXT_INITIALIZATION_MIN_PERCENT + percent);
    }

    public void applicationFrameInitializing(ApplicationFrame applicationFrame) {
        updateSplash(APPLICATION_STATUS_INITIALIZING_FRAME, CONTEXT_INITIALIZATION_MAX_PERCENT);
    }

    @Override
    public void applicationFrameInitialized(ApplicationFrame applicationFrame) {
        updateSplash(APPLICATION_STATUS_STARTED, 100);
    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {
        splash.close();
    }

    /**
     * Log the startup Progress and update the splash.
     *
     * @param status  the new status
     * @param percent the new progress percent
     */
    protected void updateSplash(String status, int percent) {
        application.logStartupProgress(percent, status);
        splash.update(status, percent);
    }
}
