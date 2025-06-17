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

package org.cosinus.swing.window;

import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.context.ApplicationProperties;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.form.FormComponent;
import org.cosinus.swing.menu.MenuBar;
import org.cosinus.swing.menu.MenuProvider;
import org.cosinus.swing.resource.DefaultResourceResolver;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.context.ApplicationContextInjector.applicationContext;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Wrapper over a {@link JFrame}
 * which will automatically inject the application context.
 */
public class Frame extends JFrame implements Window, FormComponent {

    @Autowired
    protected ActionController actionController;

    @Autowired
    protected Translator translator;

    @Autowired
    protected ErrorHandler errorHandler;

    @Autowired
    protected DefaultResourceResolver resourceResolver;

    @Autowired
    protected WindowSettingsHandler windowSettingsHandler;

    @Autowired
    protected MenuProvider menuProvider;

    @Autowired
    protected ApplicationUIHandler uiHandler;

    @Autowired
    protected ApplicationProperties applicationProperties;

    private WindowSettings windowSettings;

    private MenuBar menuBar;

    public Frame() {
        this(null);
    }

    public Frame(WindowSettings windowSettings) {
        if (applicationContext != null) {
            initialize();
        }
        this.windowSettings = windowSettings;
    }

    protected void initialize() {
        injectContext(this);
        if (windowSettings == null) {
            windowSettings = new WindowSettings(
                ofNullable(applicationProperties.getFrame().getName())
                    .orElseGet(this::getWindowName),
                applicationProperties.getName(),
                applicationProperties.getIcon(),
                applicationProperties.getMenu());
        }

        windowSettingsHandler.loadWindowSettings(windowSettings);

        initFrameNameAndIcon();
        initPositionAndSize();
        initFrameBasicActions();
    }

    private void initPositionAndSize() {
        setWindowPositionAndSize(windowSettings, uiHandler.getScreenBound());
        windowSettingsHandler.saveWindowSettings(windowSettings);
    }

    private void initFrameBasicActions() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        if (windowSettings.isExitOnEscape()) {
            registerExitOnEscapeKey();
        }

        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                if (windowSettings.isMaximized()) {
                    setExtendedState(MAXIMIZED_BOTH);
                }
            }
        });

        addComponentListener(new ComponentListener() {
            public void componentHidden(ComponentEvent e) {
            }

            public void componentMoved(ComponentEvent e) {
                if (0 == (getExtendedState() & MAXIMIZED_BOTH)) {
                    windowSettings
                        .setPosition(getX(), getY())
                        .setCentered(false);
                    windowSettingsHandler.saveWindowSettings(windowSettings);
                }
            }

            public void componentResized(ComponentEvent e) {
                if (0 == (getExtendedState() & MAXIMIZED_BOTH)) {
                    windowSettings
                        .setSize(getWidth(), getHeight())
                        .setCentered(false);
                    windowSettingsHandler.saveWindowSettings(windowSettings);
                }
            }

            public void componentShown(ComponentEvent e) {
            }
        });

        addWindowStateListener(e -> {
            if (0 != (e.getNewState() & MAXIMIZED_BOTH)) {
                windowSettings.setMaximized(true);
                windowSettings.resetOldPositionSize();
            } else if (e.getNewState() == NORMAL) {
                windowSettings.setMaximized(false);
            }
            windowSettingsHandler.saveWindowSettings(windowSettings);
        });
    }

    private void initFrameNameAndIcon() {
        setTitle(windowSettings.getTitle());
        ofNullable(windowSettings.getIcon())
            .flatMap(resourceResolver::resolveImageAsBytes)
            .map(ImageIcon::new)
            .map(ImageIcon::getImage)
            .ifPresent(this::setIconImage);
    }

    private void initFrameMenu() {
        try {
            menuProvider.getMenu(windowSettings.getMenu())
                .ifPresent(menuModel -> {
                    menuBar = new MenuBar(menuModel,
                        menuProvider.hasBoxMenu(),
                        actionController);
                    menuBar.initComponents();
                    setJMenuBar(menuBar);
                });
        } catch (Exception ex) {
            errorHandler.handleError(ex);
        }
    }

    public String translate(String key) {
        return translator.translate(key);
    }

    public void showError(Throwable error) {
        errorHandler.handleError(this, error);
    }

    public void showError(String errorMessage) {
        errorHandler.handleError(this, errorMessage);
    }

    public MenuBar getMenu() {
        return menuBar;
    }

    @Override
    public void initComponents() {
        try {
            initFrameMenu();
        } catch (Exception ex) {
            errorHandler.handleError(ex);
        }
    }

    @Override
    public void translate() {
        if (menuBar != null) {
            menuBar.translate();
        }
    }
}
