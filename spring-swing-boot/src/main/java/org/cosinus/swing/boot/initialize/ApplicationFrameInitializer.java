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

package org.cosinus.swing.boot.initialize;

import org.cosinus.swing.boot.ApplicationFrame;

import static javax.swing.SwingUtilities.invokeLater;

/**
 * Implementation of {@link ApplicationInitializer} for initializing the Swing Application frame.
 * This will trigger the initialization of the application frame after the application is started.
 */
public class ApplicationFrameInitializer implements ApplicationInitializer {

    private final ApplicationFrame applicationFrame;

    public ApplicationFrameInitializer(ApplicationFrame applicationFrame) {
        this.applicationFrame = applicationFrame;
    }

    /**
     * Show main application frame and asynchronously load the content of the frame.
     */
    @Override
    public void initialize() {
        applicationFrame.showApplicationFrame();
        invokeLater(applicationFrame::loadApplicationFrame);
    }
}
