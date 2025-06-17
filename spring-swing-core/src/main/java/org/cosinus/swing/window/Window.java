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

import org.cosinus.swing.action.EscapeActionListener;
import org.cosinus.swing.translate.Translatable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Optional;

import static java.awt.Toolkit.getDefaultToolkit;
import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import static javax.swing.KeyStroke.getKeyStroke;
import static org.cosinus.swing.window.WindowSettings.DEFAULT_HEIGHT;
import static org.cosinus.swing.window.WindowSettings.DEFAULT_WIDTH;

/**
 * Generic window interface
 */
public interface Window extends Translatable {

    /**
     * Register the exit on escape key for this window
     */
    default void registerExitOnEscapeKey() {
        getComponent().ifPresent(component -> component
            .registerKeyboardAction(new EscapeActionListener(this),
                                    getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                    WHEN_IN_FOCUSED_WINDOW));
    }

    /**
     * Get the first component of this window.
     *
     * @return the first component
     */
    default Optional<JComponent> getComponent() {
        return stream(awtWindow().getComponents())
            .filter(comp -> JComponent.class.isAssignableFrom(comp.getClass()))
            .map(JComponent.class::cast)
            .findFirst();
    }

    /**
     * Center this window on the current screen bounds
     */
    default void centerWindow() {
        java.awt.Window window = awtWindow();
        ofNullable(window.getParent())
            .ifPresentOrElse(window::setLocationRelativeTo, () -> {
                Dimension screenSize = getDefaultToolkit().getScreenSize();
                window.setLocation((screenSize.width - window.getWidth()) / 2,
                    (screenSize.height - window.getHeight()) / 2);
            });
    }

    /**
     * Get this window.
     *
     * @return this
     */
    default java.awt.Window awtWindow() {
        return (java.awt.Window) this;
    }

    /**
     * Get the window name from class name.
     *
     * @return the window name
     */
    default String getWindowName() {
        return getClass().getSimpleName().split("\\$\\$")[0].toLowerCase();
    }

    /**
     * Set window position and size.
     *
     * @param windowSettings the window settings to read position and size
     * @param screenBound the screen bounds
     */
    default void setWindowPositionAndSize(WindowSettings windowSettings, Rectangle screenBound) {
        if (windowSettings.isCentered()) {
            centerWindow();
        } else {
            awtWindow().setLocation(windowSettings.getX(), windowSettings.getY());
        }

        int defaultWith = awtWindow().getWidth() > 0 ? awtWindow().getWidth() : DEFAULT_WIDTH;
        int defaultHeight = awtWindow().getHeight() > 0 ? awtWindow().getHeight() : DEFAULT_HEIGHT;
        awtWindow().setSize(windowSettings.getWidth(), windowSettings.getHeight());
        if (!screenBound.contains(awtWindow().getBounds())) {
            awtWindow().setSize(defaultWith, defaultHeight);
            centerWindow();
        }
    }

    default void cancel() {
        awtWindow().dispose();
    }
}
