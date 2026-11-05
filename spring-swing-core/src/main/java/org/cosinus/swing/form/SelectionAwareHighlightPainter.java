/*
 * Copyright 2026 Cosinus Software
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

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position.Bias;
import javax.swing.text.View;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Highlight painter which doesn't fill the selected part of the highlighted text,
 * but only marks it with a rectangle, so that a selected text is seen as selected, and also as highlighted.
 * <p>
 * The highlights are painted after the selection, which they would cover otherwise.
 * The rectangle has the color of the highlight, or the selected text color when the highlight has the selection color
 * (e.g. the current found text), so that it is seen on the selection.
 */
public class SelectionAwareHighlightPainter extends DefaultHighlightPainter {

    public SelectionAwareHighlightPainter(Color color) {
        super(color);
    }

    @Override
    public void paint(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c) {
        int selectionStart = c.getSelectionStart();
        int selectionEnd = c.getSelectionEnd();
        if (!overlaps(offs0, offs1, selectionStart, selectionEnd)) {
            super.paint(g, offs0, offs1, bounds, c);
            return;
        }
        if (offs0 < selectionStart) {
            super.paint(g, offs0, selectionStart, bounds, c);
        }
        if (selectionEnd < offs1) {
            super.paint(g, selectionEnd, offs1, bounds, c);
        }
        markSelected(g, Math.max(offs0, selectionStart), Math.min(offs1, selectionEnd), c);
    }

    @Override
    public Shape paintLayer(Graphics g, int offs0, int offs1, Shape bounds, JTextComponent c, View view) {
        int selectionStart = c.getSelectionStart();
        int selectionEnd = c.getSelectionEnd();
        if (!overlaps(offs0, offs1, selectionStart, selectionEnd)) {
            return super.paintLayer(g, offs0, offs1, bounds, c, view);
        }
        Shape before = offs0 < selectionStart ?
            super.paintLayer(g, offs0, selectionStart, bounds, c, view) :
            null;
        Shape after = selectionEnd < offs1 ?
            super.paintLayer(g, selectionEnd, offs1, bounds, c, view) :
            null;
        Shape selected = markSelected(g, Math.max(offs0, selectionStart), Math.min(offs1, selectionEnd),
            bounds, c, view);
        return union(union(before, after), selected);
    }

    /**
     * Mark the selected part of the highlighted text in a view (i.e. on a line) with a rectangle.
     *
     * @return the marked area, or null if it cannot be computed
     */
    private Shape markSelected(Graphics g, int start, int end, Shape bounds, JTextComponent c, View view) {
        try {
            Shape shape = start == view.getStartOffset() && end == view.getEndOffset() ?
                bounds :
                view.modelToView(start, Bias.Forward, end, Bias.Backward, bounds);
            Rectangle area = shape.getBounds();
            drawRectangle(g, area, c);
            return area;
        } catch (BadLocationException ex) {
            return null;
        }
    }

    /**
     * Mark the selected part of the highlighted text with a rectangle per line.
     */
    private void markSelected(Graphics g, int start, int end, JTextComponent c) {
        try {
            Rectangle2D first = c.modelToView2D(start);
            Rectangle2D last = c.modelToView2D(end);
            if (first.getY() == last.getY()) {
                drawRectangle(g, first.createUnion(last).getBounds(), c);
            } else {
                Rectangle visible = c.getVisibleRect();
                int right = visible.x + visible.width;
                drawRectangle(g, new Rectangle((int) first.getX(), (int) first.getY(),
                    right - (int) first.getX(), (int) (last.getY() - first.getY())), c);
                drawRectangle(g, new Rectangle(visible.x, (int) last.getY(),
                    (int) last.getX() - visible.x, (int) last.getHeight()), c);
            }
        } catch (BadLocationException ex) {
            // nothing to mark
        }
    }

    private void drawRectangle(Graphics g, Rectangle area, JTextComponent c) {
        if (area.width <= 0 || area.height <= 0) {
            return;
        }
        g.setColor(markColor(c));
        g.drawRect(area.x, area.y, area.width - 1, area.height - 1);
    }

    private Color markColor(JTextComponent c) {
        Color color = getColor() != null ? getColor() : c.getSelectionColor();
        return color.equals(c.getSelectionColor()) ? c.getSelectedTextColor() : color;
    }

    private boolean overlaps(int offs0, int offs1, int selectionStart, int selectionEnd) {
        return selectionStart < selectionEnd && selectionStart < offs1 && offs0 < selectionEnd;
    }

    /**
     * @return the painted area, used by the highlighter to repaint the highlight when it changes
     */
    private Shape union(Shape first, Shape second) {
        if (first == null) {
            return second;
        }
        if (second == null) {
            return first;
        }
        return first.getBounds().union(second.getBounds());
    }
}
