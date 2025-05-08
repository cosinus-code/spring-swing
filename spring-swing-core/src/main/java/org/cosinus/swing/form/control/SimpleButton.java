/*
 *
 *  * Copyright 2024 Cosinus Software
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 *
 */

package org.cosinus.swing.form.control;

import org.cosinus.swing.color.Colors;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.color.SystemColor.BUTTON_BACKGROUND;

public class SimpleButton extends Label implements MouseListener {

    @Autowired
    private ApplicationUIHandler uiHandler;

    private Runnable runnable;

    public SimpleButton(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
        initComponent();
    }

    public SimpleButton(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
        initComponent();
    }

    public SimpleButton(String text) {
        super(text);
        initComponent();
    }

    public SimpleButton(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
        initComponent();
    }

    public SimpleButton(Icon image) {
        super(image);
        initComponent();
    }

    public SimpleButton() {
        super();
        initComponent();
    }

    private void initComponent() {
        setOpaque(true);
        addMouseListener(this);
    }

    public void addAction(final Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        runnable.run();
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        ofNullable(getBackground())
            .map(Colors::getLighterColor)
            .ifPresent(this::setBackground);
        setBorder(uiHandler.getBorder("TitledBorder.border"));
    }

    @Override
    public void mouseExited(MouseEvent e) {
        setBackground(uiHandler.getColor(BUTTON_BACKGROUND));
        setBorder(null);
    }
}
