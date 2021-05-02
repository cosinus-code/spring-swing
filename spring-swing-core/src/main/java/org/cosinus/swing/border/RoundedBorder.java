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

package org.cosinus.swing.border;

import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * Rounded border implementation
 */
public class RoundedBorder extends AbstractBorder {

    private static final long serialVersionUID = -8436105005184897096L;

    private final Color color;

    private final Insets insets;

    public RoundedBorder(Color color, Insets insets) {
        this.color = color;
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

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Color oldColor = g.getColor();
        g.setColor(color);

        g.drawRect(x, y, width - 1, height - 1);
        g.drawRoundRect(x, y, width - 1, height - 1, 5, 5);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawRoundRect(x, y, width - 1, height - 1, 10, 10);

        g.setColor(oldColor);
    }
}
