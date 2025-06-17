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

package org.cosinus.swing.form;

import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static org.cosinus.swing.color.SystemColor.MENU_SELECTION_BACKGROUND;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Rounded custom progress bar ui
 */
public class CustomProgressBarUI extends BasicProgressBarUI {

    @Autowired
    private ApplicationUIHandler uiHandler;

    public CustomProgressBarUI() {
        injectContext(this);
    }

    @Override
    protected void paintIndeterminate(Graphics g, JComponent c) {
        Rectangle rectangle = new Rectangle();
        getBox(rectangle);
        paintProgress(g, rectangle);
    }

    @Override
    protected void paintDeterminate(Graphics g, JComponent c)
    {
        Insets b = progressBar.getInsets(); // area for border
        int barRectWidth = progressBar.getWidth() - (b.right + b.left);
        int barRectHeight = progressBar.getHeight() - (b.top + b.bottom);

        if (barRectWidth <= 0 || barRectHeight <= 0) {
            return;
        }

        int amountFull = getAmountFull(b, barRectWidth, barRectHeight);

        Rectangle rectangle = new Rectangle(b.left, b.top,
            amountFull + b.left, barRectHeight + b.top);

        paintProgress(g, rectangle);
    }

    private void paintProgress(final Graphics g, Rectangle rectangle) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g2d.setColor(uiHandler.getColor(MENU_SELECTION_BACKGROUND));
        g2d.fillRoundRect(rectangle.x, rectangle.y, rectangle.width, rectangle.height - 1, 10, 10);
    }
}
