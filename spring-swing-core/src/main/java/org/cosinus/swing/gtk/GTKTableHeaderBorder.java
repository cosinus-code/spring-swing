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

package org.cosinus.swing.gtk;

import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 *
 */
public class GTKTableHeaderBorder extends AbstractBorder {

    private boolean over, pressed;

    private final Insets insets;

    public GTKTableHeaderBorder(Insets insets) {
        this.insets = insets;
    }

    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = this.insets.left;
        insets.top = this.insets.top;
        insets.right = this.insets.right;
        insets.bottom = this.insets.bottom;
        return insets;
    }

    public void updateBorder(boolean pressed, boolean over) {
        this.over = over;
        this.pressed = pressed;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        int bottom = 4;
        int top = 0;

        // top
        g.setColor(GTKColors.getVertical2());
        g.drawLine(0, 0, width, 0);

        // bottom
        g.setColor(GTKColors.getTableHeaderBottomBorderColor(1, pressed, over));
        g.drawLine(0, height - bottom, width, height - bottom);
        g.setColor(GTKColors.getTableHeaderBottomBorderColor(2, pressed, over));
        g.drawLine(0, height - bottom + 1, width, height - bottom + 1);
        g.setColor(GTKColors.getTableHeaderBottomBorderColor(3, pressed, over));
        g.drawLine(0, height - bottom + 2, width, height - bottom + 2);
        g.setColor(GTKColors.getTableHeaderBottomBorderColor(4, pressed, over));
        g.drawLine(0, height - bottom + 3, width, height - bottom + 3);

        g.setColor(GTKColors.getVertical1());
        g.drawLine(0, top, 0, height - 1);
        g.setColor(GTKColors.getVertical2());
        g.drawLine(width - 1, top, width - 1, height - 1);
        g.setColor(GTKColors.getVertical1());
        g.drawLine(width, top, width, height - 1);
    }
}
