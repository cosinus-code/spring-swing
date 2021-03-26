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

package org.cosinus.swing.form.control;

import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import static org.cosinus.swing.border.Borders.borderWithMargin;
import static org.cosinus.swing.border.Borders.lineBorder;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;
import static org.cosinus.swing.util.FontUtils.getFontDescription;

public class FontLabel extends JLabel implements Control<Font>, MouseListener {

    @Autowired
    private DialogHandler dialogHandler;

    @Autowired
    private ApplicationUIHandler uiHandler;

    private Font font;

    public FontLabel(Font font) {
        injectContext(this);

        setValue(font);
        setBorder(borderWithMargin(lineBorder(uiHandler.getColor("controlHighlight")),
                                   3, 5, 3, 5));
        addMouseListener(this);
    }

    @Override
    public Font getValue() {
        return this.font;
    }

    @Override
    public void setValue(Font font) {
        this.font = font;
        setText(getFontDescription(font));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        dialogHandler.chooseFont(font)
            .ifPresent(this::setValue);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
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
