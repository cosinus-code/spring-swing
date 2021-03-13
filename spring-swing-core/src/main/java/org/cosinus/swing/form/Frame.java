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

import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.context.ApplicationProperties;
import org.cosinus.swing.context.SwingApplicationContext;
import org.cosinus.swing.context.SwingInject;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.menu.MenuBar;
import org.cosinus.swing.menu.MenuProvider;
import org.cosinus.swing.resource.ResourceResolver;
import org.cosinus.swing.translate.Translator;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.form.WindowSettings.DEFAULT_HEIGHT;
import static org.cosinus.swing.form.WindowSettings.DEFAULT_WIDTH;

/**
 * Abstract frame window with basic functionality
 */
public class Frame extends JFrame implements Window, SwingInject, FormComponent {

    @Autowired
    protected ActionController<?> actionController;

    @Autowired
    protected Translator translator;

    @Autowired
    protected ErrorHandler errorHandler;

    @Autowired
    protected ResourceResolver resourceResolver;

    @Autowired
    protected WindowSettingsHandler frameSettingsHandler;

    @Autowired
    protected MenuProvider menuProvider;

    @Autowired
    protected ApplicationUIHandler uiHandler;

    @Autowired
    protected ApplicationProperties applicationProperties;

    private WindowSettings frameSettings;

    private MenuBar menuBar;

    protected Frame() {
        ofNullable(SwingApplicationContext.instance)
            .ifPresent(this::init);
    }

    protected Frame(WindowSettings frameSettings) {
        this.frameSettings = frameSettings;
        ofNullable(SwingApplicationContext.instance)
            .ifPresent(this::init);
    }

    public void init(SwingApplicationContext swingContext) {
        injectSwingContext(swingContext);
        init();
    }

    public void init() {
        if (frameSettings == null) {
            frameSettings = new WindowSettings(
                ofNullable(applicationProperties.getFrame().getName())
                    .orElseGet(this::getClassName),
                applicationProperties.getName(),
                applicationProperties.getIcon(),
                applicationProperties.getMenu());
        }

        frameSettingsHandler.loadWindowSettings(frameSettings);

        initFrameNameAndIcon();
        initFramePositionAndSize();
        initFrameBasicActions();
    }

    private String getClassName() {
        return getClass().getSimpleName().split("\\$\\$")[0].toLowerCase();
    }

    private void initFramePositionAndSize() {
        setSize(frameSettings.getWidth(), frameSettings.getHeight());
        if (frameSettings.isCentered()) {
            centerWindow();
        } else {
            setLocation(frameSettings.getX(), frameSettings.getY());
        }

        if (!uiHandler.getGraphicsDevicesBound().contains(getBounds())) {
            setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
            centerWindow();
        }
        frameSettingsHandler.saveWindowSettings(frameSettings);
    }

    private void initFrameBasicActions() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        if (frameSettings.isExitOnEscape()) {
            registerExitOnEscapeKey();
        }

        addWindowListener(new WindowAdapter() {
            public void windowOpened(WindowEvent e) {
                if (frameSettings.isMaximized()) {
                    setExtendedState(Frame.MAXIMIZED_BOTH);
                }
            }
        });

        addComponentListener(new ComponentListener() {
            public void componentHidden(ComponentEvent e) {
            }

            public void componentMoved(ComponentEvent e) {
                if (0 == (getExtendedState() & MAXIMIZED_BOTH)) {
                    frameSettings
                        .setPosition(getX(), getY())
                        .setCentered(false);
                    frameSettingsHandler.saveWindowSettings(frameSettings);
                }
            }

            public void componentResized(ComponentEvent e) {
                if (0 == (getExtendedState() & MAXIMIZED_BOTH)) {
                    frameSettings
                        .setSize(getWidth(), getHeight())
                        .setCentered(false);
                    frameSettingsHandler.saveWindowSettings(frameSettings);
                }
            }

            public void componentShown(ComponentEvent e) {
            }
        });

        addWindowStateListener(e -> {
            if (0 != (e.getNewState() & MAXIMIZED_BOTH)) {
                frameSettings.setMaximized(true);
                frameSettings.resetOldPositionSize();
            } else if (e.getNewState() == NORMAL) {
                frameSettings.setMaximized(false);
            }
            frameSettingsHandler.saveWindowSettings(frameSettings);
        });
    }

    private void initFrameNameAndIcon() {
        setTitle(frameSettings.getTitle());
        Optional.ofNullable(frameSettings.getIcon())
            .flatMap(resourceResolver::resolveImageAsBytes)
            .map(ImageIcon::new)
            .map(ImageIcon::getImage)
            .ifPresent(this::setIconImage);
    }

    private void initFrameMenu() {
        menuProvider.getMenu(frameSettings.getMenu())
            .ifPresent(menuModel -> {
                menuBar = new MenuBar(menuModel,
                                      menuProvider.hasBoxMenu(),
                                      actionController);
                menuBar.initComponents();
                setJMenuBar(menuBar);
            });
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

    @Override
    public void initComponents() {
        initFrameMenu();
    }

    @Override
    public void initContent() {
    }

    @Override
    public void translate() {
        if (menuBar != null) {
            menuBar.translate();
        }
    }
}
