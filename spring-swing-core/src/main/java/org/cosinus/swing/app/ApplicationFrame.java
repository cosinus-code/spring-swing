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

import org.cosinus.swing.boot.SwingApplicationContext;
import org.cosinus.swing.boot.event.ApplicationFrameAfterInitializeEvent;
import org.cosinus.swing.boot.event.ApplicationFrameBeforeInitializeEvent;
import org.cosinus.swing.form.Frame;
import org.cosinus.swing.form.WindowSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Main frame of the application
 */
public class ApplicationFrame extends Frame {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    public void start() {
        applicationEventPublisher.publishEvent(new ApplicationFrameBeforeInitializeEvent(this));
        super.init();
        applicationEventPublisher.publishEvent(new ApplicationFrameAfterInitializeEvent(this));
        setVisible(true);
    }
}
