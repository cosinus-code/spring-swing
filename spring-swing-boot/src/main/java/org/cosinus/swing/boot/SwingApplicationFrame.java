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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.boot.event.ApplicationFrameAfterInitializeEvent;
import org.cosinus.swing.boot.event.ApplicationFrameBeforeInitializeEvent;
import org.cosinus.swing.context.ApplicationProperties;
import org.cosinus.swing.ui.listener.UIChangeController;
import org.cosinus.swing.ui.listener.UIChangeListener;
import org.cosinus.swing.window.Frame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

/**
 * Implementation of {@link ApplicationFrame} as Swing {@link Frame}.
 * <p>
 * Keeping this application frame instance as static field is a compromise
 * to avoid circular dependencies when the main frame is needed as parent for other windows.
 */
public abstract class SwingApplicationFrame extends Frame implements ApplicationFrame, UIChangeListener {

    private static final Logger LOG = LogManager.getLogger(SwingApplicationFrame.class);

    public static Frame applicationFrame;

    @Autowired
    public ApplicationProperties applicationProperties;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired(required = false)
    protected UIChangeController uiChangeController;

    /**
     * Initialize the application frame.
     * <p>
     * It also publish the {@link ApplicationFrameBeforeInitializeEvent} and
     * {@link ApplicationFrameAfterInitializeEvent} before and after initialization.
     */
    @Override
    public void initApplicationFrame() {
        LOG.info("Initializing application frame...");
        applicationEventPublisher.publishEvent(new ApplicationFrameBeforeInitializeEvent(this));
        initialize();
        applicationEventPublisher.publishEvent(new ApplicationFrameAfterInitializeEvent(this));

        applicationFrame = this;
        if (uiChangeController != null) {
            uiChangeController.registerUIChangeListener(this);
        }
    }

    /**
     * Initialize frame swing components and show the frame.
     */
    @Override
    public void showApplicationFrame() {
        initComponents();
        triggerFormUpdate();

        LOG.info("Showing application frame...");
        setVisible(true);
    }

    /**
     * Load the application frame content and do eventual translations.
     */
    @Override
    public void loadApplicationFrame() {
        LOG.info("Loading application frame...");
        loadContent();
        translate();
    }

    @Override
    public void uiThemeChanged() {
        triggerFormUpdate();
        LOG.info("Form updated due to UI theme change");
    }

    protected abstract void loadContent();
}
