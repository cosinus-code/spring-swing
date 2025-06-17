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

package org.cosinus.swing.boot;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ResourceLoader;

import java.util.function.Supplier;

import static java.lang.String.format;
import static javax.swing.SwingUtilities.invokeLater;
import static org.springframework.boot.Banner.Mode.OFF;
import static org.springframework.boot.WebApplicationType.NONE;

/**
 * Swing extension of the {@link SpringApplication}.
 * <p>
 * As a Swing application it is not web and not headless.
 * To cut as much the load at startup, also the banner mode if off.
 * <p>
 * Keeping the application class as static field is a compromise
 * for rare cases when you need an unique class which identify the application
 */
public class SpringSwingApplication extends SpringApplication {

    public static Class<?> applicationClass;

    private boolean logStartupProgress;

    public SpringSwingApplication(Class<?>... sources) {
        this(null, sources);
    }

    public SpringSwingApplication(ResourceLoader resourceLoader, Class<?>... sources) {
        super(resourceLoader, sources);
        setHeadless(false);
        setWebApplicationType(NONE);
        setBannerMode(OFF);
    }

    public static ConfigurableApplicationContext run(Class<?> appClass,
                                                     String[] args) {
        applicationClass = appClass;
        return new SpringSwingApplicationBuilder(appClass).run(args);
    }

    /**
     * After the context is refreshed, start initializing the application frame.
     *
     * @param context the application context
     * @param args    the application arguments
     */
    @Override
    protected void afterRefresh(ConfigurableApplicationContext context, ApplicationArguments args) {
        super.afterRefresh(context, args);
        invokeLater(() -> initApplicationFrame(context));
    }

    /**
     * Start initializing the application frame.
     * <p>
     * The {@link ApplicationFrame} bean should be already in context.
     * If no {@link ApplicationFrame} was declared in for the application
     * this will fail and application will not start.
     *
     * @param context the application context
     */
    protected void initApplicationFrame(ConfigurableApplicationContext context) {
        context.getBean(ApplicationFrame.class).initApplicationFrame();
    }

    /**
     * Log the application startup progress.
     *
     * @param percent the current status
     * @param statusSupplier the current status supplier
     */
    public void logStartupProgress(int percent, Supplier<String> statusSupplier) {
        logStartupProgress(percent, statusSupplier.get());
    }

    /**
     * Log the application startup progress.
     * <p>
     * If the application is started with -log-startup-progress argument,
     * the the whole startup progress will be logged by this method.
     *
     * @param percent the current percent
     * @param status the current status
     */
    public void logStartupProgress(int percent, String status) {
        if (this.logStartupProgress) {
            getApplicationLog().info(format("Application startup %d%% -> %s", percent, status));
        }
    }

    /**
     * Set if the application should log the startup progress.
     *
     * @param logStartupProgress true if the application should log the startup progress
     */
    public void setLogStartupProgress(boolean logStartupProgress) {
        this.logStartupProgress = logStartupProgress;
    }
}
