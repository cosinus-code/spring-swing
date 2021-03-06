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

package org.cosinus.swing.boot;

import org.cosinus.swing.util.ReflectionUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import static org.cosinus.swing.util.ReflectionUtils.createBean;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactoryNames;

/**
 * Builder for {@link SpringSwingApplication}
 */
public class SpringSwingApplicationBuilder extends SpringApplicationBuilder {

    private final ApplicationStartupListeners startupListeners = new ApplicationStartupListeners();

    public SpringSwingApplicationBuilder(Class<?>... sources) {
        super(sources);
    }

    /**
     * Creates the {@link SpringSwingApplication} instance.
     *
     * @param sources the application class sources
     * @return the created swing application
     */
    @Override
    protected SpringSwingApplication createSpringApplication(Class<?>... sources) {
        return new SpringSwingApplication(sources);
    }

    /**
     * Create an application context with the command line args provided.
     *
     * @param args the application arguments
     * @return the created application context
     */
    @Override
    public ConfigurableApplicationContext run(String... args) {
        loadSpringFactoriesStartupListeners(args);
        listeners(startupListeners.listeners());
        return super.run(args);
    }

    /**
     * Accessor for the current application as {@link SpringSwingApplication}
     *
     * @return the current swing application
     */
    @Override
    public SpringSwingApplication application() {
        return (SpringSwingApplication) super.application();
    }

    /**
     * Set if the application should log the startup progress.
     *
     * @param logStartupProgress true if the application should log the startup progress
     * @return this
     */
    public SpringSwingApplicationBuilder logStartupProgress(boolean logStartupProgress) {
        application().setLogStartupProgress(logStartupProgress);
        return this;
    }

    /**
     * Register startup listeners.
     *
     * @param startupListeners startup listeners to register
     * @return this
     */
    public SpringSwingApplicationBuilder startupListeners(SwingSpringApplicationStartupListener... startupListeners) {
        this.startupListeners.register(startupListeners);
        return this;
    }

    /**
     * Instantiate and register all {@link SwingSpringApplicationStartupListener} listeners
     * found in META-INF/spring.factories.
     *
     * @param args the application arguments
     */
    protected void loadSpringFactoriesStartupListeners(String[] args) {
        loadFactoryNames(SwingSpringApplicationStartupListener.class,
                         application().getClassLoader())
            .stream()
            .map(name -> ReflectionUtils.getClassForName(name, application().getClassLoader()))
            .filter(SwingSpringApplicationStartupListener.class::isAssignableFrom)
            .map(instanceClass -> createBean(instanceClass,
                                             new Class<?>[]{SpringApplication.class, String[].class},
                                             new Object[]{application(), args}))
            .map(SwingSpringApplicationStartupListener.class::cast)
            .sorted(AnnotationAwareOrderComparator.INSTANCE)
            .forEach(startupListeners::register);
    }
}
