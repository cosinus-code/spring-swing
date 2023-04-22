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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.SpringFactoriesLoader.ArgumentResolver;
import org.springframework.core.log.LogMessage;

import static org.springframework.core.io.support.SpringFactoriesLoader.FailureHandler.handleMessage;
import static org.springframework.core.io.support.SpringFactoriesLoader.forDefaultResourceLocation;

/**
 * Builder for {@link SpringSwingApplication}
 */
public class SpringSwingApplicationBuilder extends SpringApplicationBuilder {

    private static final Logger LOG = LogManager.getLogger(SpringSwingApplicationBuilder.class);

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
    protected SpringSwingApplication createSpringApplication(ResourceLoader resourceLoader, Class<?>... sources) {
        return new SpringSwingApplication(resourceLoader, sources);
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
        forDefaultResourceLocation(application().getClassLoader())
            .load(SwingSpringApplicationStartupListener.class,
                ArgumentResolver
                    .of(SpringApplication.class, application())
                    .and(String[].class, args),
                handleMessage((messageSupplier, failure) -> LOG.error(LogMessage.of(messageSupplier), failure)))
            .forEach(startupListeners::register);
    }
}
