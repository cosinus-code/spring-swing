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

package org.cosinus.swing.form.control;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static java.awt.Color.black;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.border.Borders.lineBorder;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Extension of a {@link JLabel} used for controlling a {@link Color} value.
 * <p>
 * The label is used to show the color and the {@link JColorChooser} is used to change the color,
 * when the label is clicked.
 */
public class ColorLabel extends JLabel implements Control<Color>, MouseListener {

    public ColorLabel(Color color) {
        injectContext(this);

        setOpaque(true);
        setBackground(color);
        setBorder(lineBorder(black));
        addMouseListener(this);
    }

    @Override
    public Color getControlValue() {
        return getBackground();
    }

    @Override
    public void setControlValue(Color color) {
        setBackground(color);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        ofNullable(JColorChooser.showDialog(this, "", getBackground()))
            .ifPresent(this::setBackground);
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
