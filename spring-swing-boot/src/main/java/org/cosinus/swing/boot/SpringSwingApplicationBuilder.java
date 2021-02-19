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
import org.springframework.core.io.support.SpringFactoriesLoader;

/**
 * Builder for {@link SpringSwingApplication}
 */
public class SpringSwingApplicationBuilder extends SpringApplicationBuilder {

    private final ApplicationStartupListeners startupListeners = new ApplicationStartupListeners();

    public SpringSwingApplicationBuilder(Class<?>... sources) {
        super(sources);
    }

    @Override
    protected SpringApplication createSpringApplication(Class<?>... sources) {
        return new SpringSwingApplication(sources);
    }

    @Override
    public ConfigurableApplicationContext run(String... args) {
        loadSpringFactoriesStartupListeners(args);
        listeners(startupListeners.listeners());
        return super.run(args);
    }

    @Override
    public SpringSwingApplication application() {
        return (SpringSwingApplication) super.application();
    }

    public SpringSwingApplicationBuilder logStartupProgress(boolean logStartupProgress) {
        application().setLogStartupProgress(logStartupProgress);
        return this;
    }

    public SpringSwingApplicationBuilder startupListeners(
            SwingSpringApplicationStartupListener... startupListeners) {
        this.startupListeners.register(startupListeners);
        return this;
    }

    protected void loadSpringFactoriesStartupListeners(String[] args) {
        Class<?>[] constructorParameterTypes = new Class<?>[]{SpringApplication.class, String[].class};
        Object[] constructorParameterValues = new Object[]{application(), args};
        SpringFactoriesLoader.loadFactoryNames(SwingSpringApplicationStartupListener.class,
                                               application().getClassLoader())
                .stream()
                .map(name -> ReflectionUtils.getClassForName(name, application().getClassLoader()))
                .filter(SwingSpringApplicationStartupListener.class::isAssignableFrom)
                .map(instanceClass -> ReflectionUtils.createBean(instanceClass,
                                                                 constructorParameterTypes,
                                                                 constructorParameterValues))
                .map(SwingSpringApplicationStartupListener.class::cast)
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .forEach(startupListeners::register);
    }
}
