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

package org.cosinus.swing.app;

import org.cosinus.swing.boot.ApplicationInitializer;

/**
 * Swing UI initializer
 */
public class ApplicationContentInitializer implements ApplicationInitializer {

    private final ApplicationFrame applicationFrame;

    public ApplicationContentInitializer(ApplicationFrame applicationFrame) {
        this.applicationFrame = applicationFrame;
    }

    @Override
    public void initialize() {
        applicationFrame.initContent();
    }
}
