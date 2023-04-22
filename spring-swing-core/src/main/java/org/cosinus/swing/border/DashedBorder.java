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
import java.io.Serial;

/**
 * Dashed border implementation
 */
public class DashedBorder extends LineBorder {

    @Serial
    private static final long serialVersionUID = 2998788302974704170L;

    private final float[] dash;

    public DashedBorder(Color color, float[] dash) {
        super(color, 1, false);
        this.dash = dash;
    }

    public DashedBorder(Color color, int thickness, float[] dash) {
        super(color, thickness, false);
        this.dash = dash;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Color oldColor = g.getColor();
        g.setColor(lineColor);

        if (roundedCorners && dash == null) {
            drawRect(g, x, y, width - 1, height - 1);
            drawRect(g, x, y, width - 1, height - 1, true, 5, 5);
        }

        Graphics2D g2d = (Graphics2D) g;
        Stroke oldStroke = g2d.getStroke();
        if (dash == null) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setStroke(new BasicStroke(thickness));
        } else {
            g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1, dash, 0));
        }

        if (roundedCorners) drawRect(g, x, y, width - 1, height - 1, true, 10, 10);
        else drawRect(g, x, y, width - 1, height - 1);

        g.setColor(oldColor);
        g2d.setStroke(oldStroke);
    }
}
