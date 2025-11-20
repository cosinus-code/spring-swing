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

import lombok.Setter;
import org.cosinus.swing.form.FormComponent;
import org.cosinus.swing.icon.IconHolder;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.color.Colors.getLighterColor;

public class NoBorderButton extends Button implements FormComponent, IconHolder {

    @Autowired
    private ApplicationUIHandler uiHandler;

    @Setter
    private String iconName;

    private Color initialBackground;

    public NoBorderButton() {
        init();
    }

    public NoBorderButton(Icon icon) {
        super(icon);
        init();
    }

    public NoBorderButton(Icon icon, Runnable action) {
        super(icon, action);
        init();
    }

    public NoBorderButton(Icon icon, ActionListener action) {
        super(icon, action);
        init();
    }

    public NoBorderButton(String text) {
        super(text);
        init();
    }

    public NoBorderButton(Action action) {
        super(action);
        init();
    }

    public NoBorderButton(String text, Icon icon) {
        super(text, icon);
        init();
    }

    public NoBorderButton(String text, Runnable action) {
        super(text, action);
        init();
    }

    public NoBorderButton(String text, ActionListener action) {
        super(text, action);
        init();
    }

    public void init() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent mouseEvent) {
                if (!getBackground().equals(initialBackground)) {
                    initialBackground = getBackground();
                }
                ofNullable(initialBackground)
                    .map(background -> getLighterColor(background, 30))
                    .ifPresent(NoBorderButton.this::setBackground);
            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                NoBorderButton.this.setBackground(initialBackground);
            }
        });
    }

    @Override
    public void updateForm() {
        initialBackground = getBackground();

        setUI(new BasicButtonUI());
        setBorder(emptyBorder(2, 5, 2, 5));
        setCursor(uiHandler.getHandCursor());
    }

    @Override
    public String getIconName() {
        return iconName;
    }
}
