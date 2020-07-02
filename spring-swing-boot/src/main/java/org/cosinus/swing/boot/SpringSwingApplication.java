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

import org.cosinus.swing.context.SpringSwingComponent;
import org.cosinus.swing.context.SwingApplicationContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.springframework.boot.Banner.Mode.OFF;
import static org.springframework.boot.WebApplicationType.NONE;

/**
 * Swing application
 */
public class SpringSwingApplication extends SpringApplication {

    private boolean logStartupProgress;

    public SpringSwingApplication(Class<?>... sources) {
        super(sources);
        setHeadless(false);
        setWebApplicationType(NONE);
        setBannerMode(OFF);
    }

    public static ConfigurableApplicationContext run(Class<?> appClass,
                                                     String[] args) {
        return new SpringSwingApplicationBuilder(appClass).run(args);
    }

    @Override
    protected void afterRefresh(ConfigurableApplicationContext context, ApplicationArguments args) {
        context.getBean(SwingApplicationContext.class)
                .setSwingComponents(context.getBeansWithAnnotation(SpringSwingComponent.class));

        SwingUtilities.invokeLater(() -> startApplicationFrame(context));
    }

    protected void startApplicationFrame(ConfigurableApplicationContext context) {
        context.getBean(ApplicationFrame.class).startApplication();
    }

    public void logStartupProgress(int percent, Supplier<String> statusSupplier) {
        logStartupProgress(percent, statusSupplier.get());
    }

    public void logStartupProgress(int percent, String status) {
        if (this.logStartupProgress) {
            getApplicationLog().info(format("Application startup %d%% -> %s", percent, status));
        }
    }

    public void setLogStartupProgress(boolean logStartupProgress) {
        this.logStartupProgress = logStartupProgress;
    }
}
