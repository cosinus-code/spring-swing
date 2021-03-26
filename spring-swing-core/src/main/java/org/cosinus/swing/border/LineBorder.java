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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

/**
 * Enhanced line border implementation
 */
public class LineBorder extends javax.swing.border.LineBorder {

    private int top;

    private int left;

    private int bottom;

    private int right;

    /**
     * Creates a new instance of DashedBorder
     */
    public LineBorder(Color color) {
        this(color, 1, false);
    }

    /**
     * Creates a new instance of DashedBorder
     */
    public LineBorder(Color color, int thickness) {
        this(color, thickness, false);
    }

    /**
     * Creates a new instance of DashedBorder
     */
    public LineBorder(Color color, int thickness, boolean roundedCorners) {
        super(color, thickness, roundedCorners);
    }

    public LineBorder(Color color, int top, int left, int bottom, int right) {
        this(color, 1, top, left, bottom, right);
    }

    public LineBorder(Color color, int thickness, int top, int left, int bottom, int right) {
        super(color, thickness, false);
        if (top != left || left != bottom || bottom != right) {
            this.top = top;
            this.left = left;
            this.bottom = bottom;
            this.right = right;
        }
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2d = (Graphics2D) g;
        Stroke oldStroke = g2d.getStroke();

        Color oldColor = g2d.getColor();
        g2d.setColor(lineColor);

        if (top > 0 || left > 0 || bottom > 0 || right > 0) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (top > 0) {
                g2d.setStroke(new BasicStroke(top));
                g2d.drawLine(x, y, x + width, y);
            }

            if (left > 0) {
                g2d.setStroke(new BasicStroke(left));
                g2d.drawLine(x, y, x, height);
            }

            if (bottom > 0) {
                g2d.setStroke(new BasicStroke(bottom));
                g2d.drawLine(x, y + height, x + width, y + height);
            }

            if (right > 0) {
                g2d.setStroke(new BasicStroke(right));
                g2d.drawLine(x + width, y, x + width, y + height);
            }
        } else {
            drawRect(g, x, y, width, height);
            drawRect(g, x, y, width, height, true, 5, 5);

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(new BasicStroke(thickness));

            if (roundedCorners) drawRect(g, x, y, width, height, true, 10, 10);
            else drawRect(g, x, y, width, height);
        }

        g.setColor(oldColor);
        g2d.setStroke(oldStroke);
    }

    protected void drawRect(Graphics g, int x, int y, int width, int height) {
        drawRect(g, x, y, width, height, false, 0, 0);
    }

    protected void drawRect(Graphics g, int x, int y, int width, int height, boolean rounded, int arcWidth, int arcHeight) {
        if (rounded) g.drawRoundRect(x, y, width, height, arcWidth, arcHeight);
        else g.drawRect(x, y, width, height);
    }
}
