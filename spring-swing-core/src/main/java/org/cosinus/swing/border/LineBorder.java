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

package org.cosinus.swing.border;

import java.awt.*;
import java.io.Serial;

/**
 * Enhanced line border implementation
 */
public class LineBorder extends javax.swing.border.LineBorder {

    @Serial
    private static final long serialVersionUID = 1266557252529296976L;

    private int top;

    private int left;

    private int bottom;

    private int right;

    private boolean rectangle;

    public LineBorder(Color color) {
        this(color, 1, false);
    }

    public LineBorder(Color color, int thickness) {
        this(color, thickness, false);
    }

    public LineBorder(Color color, int thickness, boolean roundedCorners) {
        super(color, thickness, roundedCorners);
    }

    public LineBorder(Color color, int top, int left, int bottom, int right) {
        this(color, 1, top, left, bottom, right);
    }

    public LineBorder(Color color, int thickness, int top, int left, int bottom, int right) {
        super(color, thickness, false);
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
        this.rectangle = top == left && left == bottom && bottom == right;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(lineColor);
        g2d.setStroke(new BasicStroke(thickness));
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (!rectangle) {
            if (top > 0) {
                g2d.setStroke(new BasicStroke(top));
                g2d.drawLine(x, y, x + width - 1, y);
            }

            if (left > 0) {
                g2d.setStroke(new BasicStroke(left));
                g2d.drawLine(x, y, x, y + height - 1);
            }

            if (bottom > 0) {
                g2d.setStroke(new BasicStroke(bottom));
                g2d.drawLine(x, y + height - bottom, x + width, y + height - bottom);
            }

            if (right > 0) {
                g2d.setStroke(new BasicStroke(right));
                g2d.drawLine(x + width - right, y, x + width - right, y + height);
            }
        } else if (roundedCorners) {
            g.drawRoundRect(x, y, width, height, 10, 10);
        } else {
            super.paintBorder(c, g, x, y, width, height);
        }
    }

    protected void drawRect(Graphics g, int x, int y, int width, int height) {
        drawRect(g, x, y, width, height, false, 0, 0);
    }

    protected void drawRect(Graphics g, int x, int y, int width, int height, boolean rounded, int arcWidth, int arcHeight) {
        if (rounded) {
            g.drawRoundRect(x, y, width - 1, height - 1, arcWidth, arcHeight);
        } else {
            g.drawRect(x, y, width - 1, height - 1);
        }
    }
}
