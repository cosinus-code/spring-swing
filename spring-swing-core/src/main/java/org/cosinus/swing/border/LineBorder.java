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

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Color oldColor = g.getColor();
        g.setColor(lineColor);

        drawRect(g, x, y, width - 1, height - 1);
        drawRect(g, x, y, width - 1, height - 1, true, 5, 5);

        Graphics2D g2d = (Graphics2D) g;
        Stroke oldStroke = g2d.getStroke();

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setStroke(new BasicStroke(thickness));

        if (roundedCorners) drawRect(g, x, y, width - 1, height - 1, true, 10, 10);
        else drawRect(g, x, y, width - 1, height - 1);

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
