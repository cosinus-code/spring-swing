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

import org.cosinus.swing.action.EscapeActionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Optional;

import static java.util.Arrays.stream;
import static javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW;
import static javax.swing.KeyStroke.getKeyStroke;
import static java.awt.Toolkit.getDefaultToolkit;

/**
 * Generic window interface
 */
public interface Window {

    default void registerExitOnEscapeKey() {
        getComponent().ifPresent(component -> component
            .registerKeyboardAction(new EscapeActionListener(thisWindow()),
                                    getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                                    WHEN_IN_FOCUSED_WINDOW));
    }

    default Optional<JComponent> getComponent() {
        return stream(thisWindow().getComponents())
            .filter(comp -> JComponent.class.isAssignableFrom(comp.getClass()))
            .map(JComponent.class::cast)
            .findFirst();
    }

    default void centerWindow() {
        Dimension screenSize = getDefaultToolkit().getScreenSize();
        java.awt.Window window = thisWindow();
        window.setLocation((screenSize.width - window.getWidth()) / 2,
                           (screenSize.height - window.getHeight()) / 2);
    }

    default java.awt.Window thisWindow() {
        return (java.awt.Window) this;
    }
}
