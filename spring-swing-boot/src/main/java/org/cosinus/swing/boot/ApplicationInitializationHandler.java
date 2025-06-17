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

import org.cosinus.swing.boot.initialize.ApplicationFrameInitializer;
import org.cosinus.swing.boot.initialize.ApplicationInitializer;
import org.cosinus.swing.context.ApplicationHandler;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.util.Set;

import static javax.swing.SwingUtilities.invokeLater;

/**
 * An {@link ApplicationRunner} which handle application initializers.
 * Also, as an {@link ApplicationHandler} it can reload the swing configuration
 */
public class ApplicationInitializationHandler implements ApplicationRunner, ApplicationHandler {

    private final Set<ApplicationInitializer> applicationInitializers;

    private final ApplicationFrameInitializer applicationFrameInitializer;

    public ApplicationInitializationHandler(
        final Set<ApplicationInitializer> applicationInitializers,
        final ApplicationFrameInitializer applicationFrameInitializer) {

        this.applicationInitializers = applicationInitializers;
        this.applicationFrameInitializer = applicationFrameInitializer;
    }

    @Override
    public void run(ApplicationArguments args) {
        invokeLater(this::initApplication);
    }

    /**
     * Run all {@link ApplicationInitializer} beans from application context
     */
    public void initApplication() {
        applicationInitializers.forEach(ApplicationInitializer::initialize);
        applicationFrameInitializer.initialize();
    }

    /**
     * Reload the swing configuration of the application
     */
    @Override
    public void reloadApplication() {
        run(null);
    }
}
