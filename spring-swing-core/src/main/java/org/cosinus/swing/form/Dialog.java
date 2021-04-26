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

package org.cosinus.swing.form;

import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.Frame;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.Optional;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Abstract dialog window with basic functionality
 */
public abstract class Dialog<T> extends JDialog implements Window, FormComponent {

    @Autowired
    protected WindowSettingsHandler windowSettingsHandler;

    @Autowired
    protected ApplicationUIHandler uiHandler;

    private WindowSettings windowSettings;

    private boolean cancelled;

    public Dialog(Frame frame, String title, boolean modal, boolean manageWindowSettings) {
        super(frame, title, modal);
        injectContext(this);
        if (manageWindowSettings) {
            windowSettings = createWindowSettings();
        }
    }

    public Dialog(java.awt.Dialog dialog, String title, boolean modal, boolean manageWindowSettings) {
        super(dialog, title, modal);
        injectContext(this);
        if (manageWindowSettings) {
            windowSettings = createWindowSettings();
        }
    }

    protected WindowSettings createWindowSettings() {
        WindowSettings windowSettings = new WindowSettings(getWindowName(), getTitle());
        windowSettingsHandler.loadWindowSettings(windowSettings);
        return windowSettings;
    }

    public void init() {
        initComponents();
        initPositionAndSize();
        initFrameBasicActions();
    }

    private void initPositionAndSize() {
        if (windowSettings != null) {
            setWindowPositionAndSize(windowSettings, uiHandler.getScreenBound());
            windowSettingsHandler.saveWindowSettings(windowSettings);
        }
    }

    private void initFrameBasicActions() {
        registerExitOnEscapeKey();

        if (windowSettings != null) {
            addComponentListener(new ComponentListener() {
                public void componentHidden(ComponentEvent e) {
                }

                public void componentMoved(ComponentEvent e) {
                    windowSettings
                        .setPosition(getX(), getY())
                        .setCentered(false);
                    windowSettingsHandler.saveWindowSettings(windowSettings);
                }

                public void componentResized(ComponentEvent e) {
                    windowSettings
                        .setSize(getWidth(), getHeight())
                        .setCentered(false);
                    windowSettingsHandler.saveWindowSettings(windowSettings);
                }

                public void componentShown(ComponentEvent e) {
                }
            });
        }
    }

    public Optional<T> response() {
        return isCancelled() ?
            Optional.empty() :
            Optional.ofNullable(getDialogResponse());
    }

    protected T getDialogResponse() {
        return null;
    }

    public void cancel() {
        cancelled = true;
        close();
    }

    public void close() {
        dispose();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void initComponents() {
    }

    @Override
    public void translate() {
    }
}
