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

import org.cosinus.swing.boot.initialize.ApplicationInitializer;
import org.cosinus.swing.context.ApplicationHandler;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import javax.swing.*;
import java.util.Set;

/**
 * Application initialization handler
 */
public class ApplicationInitializationHandler implements ApplicationRunner, ApplicationHandler {

    private final Set<ApplicationInitializer> applicationInitializers;

    public ApplicationInitializationHandler(Set<ApplicationInitializer> applicationInitializers) {
        this.applicationInitializers = applicationInitializers;
    }

    @Override
    public void run(ApplicationArguments args) {
        SwingUtilities.invokeLater(this::initApplication);
    }

    public void initApplication() {
        applicationInitializers.forEach(ApplicationInitializer::initialize);
    }

    @Override
    public void reloadApplication() {
        applicationInitializers.forEach(ApplicationInitializer::initialize);
    }
}
