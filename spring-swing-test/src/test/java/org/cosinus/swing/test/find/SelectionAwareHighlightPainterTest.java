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

package org.cosinus.swing.test.find;

import org.cosinus.swing.form.TextEditor;
import org.cosinus.swing.form.SelectionAwareHighlightPainter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runners.model.Statement;

import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter.HighlightPainter;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * A selected highlighted text is painted as selected, marked with a rectangle as highlighted.
 */
public class SelectionAwareHighlightPainterTest {

    private static final Color HIGHLIGHT = new Color(255, 255, 0);

    private static final Color CURRENT_HIGHLIGHT = new Color(0, 0, 255);

    private static final Color SELECTION = new Color(0, 255, 0);

    private static final Color SELECTED_TEXT = new Color(255, 0, 0);

    private static final HighlightPainter CURRENT_PAINTER = new SelectionAwareHighlightPainter(CURRENT_HIGHLIGHT);

    /**
     * The text editor is created, changed and painted on the event dispatch thread, as swing requires.
     */
    @Rule
    public TestRule onEventDispatchThread = (base, description) -> new Statement() {
        @Override
        public void evaluate() throws Throwable {
            Throwable[] failure = new Throwable[1];
            SwingUtilities.invokeAndWait(() -> {
                try {
                    base.evaluate();
                } catch (Throwable throwable) {
                    failure[0] = throwable;
                }
            });
            if (failure[0] != null) {
                throw failure[0];
            }
        }
    };

    private TextEditor textEditor;

    @Before
    public void setUp() {
        textEditor = new TextEditor() {
            @Override
            public HighlightPainter getCurrentFoundTextHighlightPainter() {
                return CURRENT_PAINTER;
            }
        };
        textEditor.setText("one two one two");
        textEditor.setSelectionColor(SELECTION);
        textEditor.setSize(400, 50);
        textEditor.highlightText(0, 3, textEditor.getFoundTextHighlightPainter());
        textEditor.highlightText(8, 11, CURRENT_PAINTER);
    }

    @Test
    public void testHighlightedTextWithoutSelection() throws BadLocationException {
        BufferedImage image = paint();
        assertTrue(inside(image, 0, 3, HIGHLIGHT) > 0);
        assertTrue(inside(image, 8, 11, CURRENT_HIGHLIGHT) > 0);
    }

    /**
     * A selected highlighted text is filled with the selection color, inside a rectangle of the highlight color.
     */
    @Test
    public void testSelectedHighlightedTextIsSelectedAndMarked() throws BadLocationException {
        select(0, 3);
        BufferedImage image = paint();
        assertEquals(0, inside(image, 0, 3, HIGHLIGHT));
        assertTrue(inside(image, 0, 3, SELECTION) > 0);
        assertMarked(image, 0, 3, HIGHLIGHT);
    }

    @Test
    public void testSelectedCurrentFoundTextIsSelectedAndMarked() throws BadLocationException {
        select(8, 11);
        BufferedImage image = paint();
        assertEquals(0, inside(image, 8, 11, CURRENT_HIGHLIGHT));
        assertTrue(inside(image, 8, 11, SELECTION) > 0);
        assertMarked(image, 8, 11, CURRENT_HIGHLIGHT);
    }

    /**
     * A highlight with the selection color is marked with the selected text color, to be seen on the selection.
     */
    @Test
    public void testHighlightWithSelectionColorIsMarkedWithSelectedTextColor() throws BadLocationException {
        textEditor.setSelectedTextColor(SELECTED_TEXT);
        textEditor.highlightText(12, 15, new SelectionAwareHighlightPainter(SELECTION));
        select(12, 15);
        assertMarked(paint(), 12, 15, SELECTED_TEXT);
    }

    /**
     * Only the selected part of a highlighted text is selected and marked.
     */
    @Test
    public void testPartlySelectedHighlightedText() throws BadLocationException {
        select(1, 6);
        BufferedImage image = paint();
        assertTrue(inside(image, 0, 1, HIGHLIGHT) > 0);
        assertEquals(0, inside(image, 1, 3, HIGHLIGHT));
        assertTrue(inside(image, 1, 3, SELECTION) > 0);
        assertMarked(image, 1, 3, HIGHLIGHT);
        assertEquals(0, inside(image, 3, 6, HIGHLIGHT));
        assertTrue(inside(image, 8, 11, CURRENT_HIGHLIGHT) > 0);
    }

    @Test
    public void testSelectionInsideHighlightedText() throws BadLocationException {
        select(1, 2);
        BufferedImage image = paint();
        assertTrue(inside(image, 0, 1, HIGHLIGHT) > 0);
        assertEquals(0, inside(image, 1, 2, HIGHLIGHT));
        assertMarked(image, 1, 2, HIGHLIGHT);
        assertTrue(inside(image, 2, 3, HIGHLIGHT) > 0);
    }

    /**
     * A highlighted text selected on several lines is marked on each line.
     */
    @Test
    public void testSelectedHighlightedTextOnSeveralLines() throws BadLocationException {
        textEditor.getHighlighter().removeAllHighlights();
        textEditor.setText("one\ntwo");
        textEditor.highlightText(0, 7, textEditor.getFoundTextHighlightPainter());
        select(0, 7);
        BufferedImage image = paint();
        assertEquals(0, inside(image, 0, 3, HIGHLIGHT));
        assertEquals(0, inside(image, 4, 7, HIGHLIGHT));
        assertMarked(image, 0, 3, HIGHLIGHT);
        assertMarked(image, 4, 7, HIGHLIGHT);
    }

    private void select(int start, int end) {
        textEditor.setCaretPosition(start);
        textEditor.moveCaretPosition(end);
        textEditor.getCaret().setSelectionVisible(true);
    }

    private BufferedImage paint() {
        BufferedImage image = new BufferedImage(textEditor.getWidth(), textEditor.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            textEditor.paint(graphics);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    /**
     * @return the number of pixels of a color in the area of a text (on a line),
     * without its border, where the rectangle marking a selected highlight is drawn
     */
    private int inside(BufferedImage image, int start, int end, Color color) throws BadLocationException {
        Rectangle area = area(start, end);
        int count = 0;
        for (int x = area.x + 1; x < area.x + area.width - 1; x++) {
            for (int y = area.y + 1; y < area.y + area.height - 1; y++) {
                if (image.getRGB(x, y) == color.getRGB()) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Assert that the area of a text (on a line) is surrounded by a rectangle of a color:
     * most of each edge has the color, as the text is drawn over the rectangle
     * (up to half of a vertical edge, for a letter next to it).
     */
    private void assertMarked(BufferedImage image, int start, int end, Color color) throws BadLocationException {
        Rectangle area = area(start, end);
        int right = area.x + area.width - 1;
        int bottom = area.y + area.height - 1;
        int top = 0, below = 0, left = 0, rightCount = 0;
        for (int x = area.x; x <= right; x++) {
            top += image.getRGB(x, area.y) == color.getRGB() ? 1 : 0;
            below += image.getRGB(x, bottom) == color.getRGB() ? 1 : 0;
        }
        for (int y = area.y; y <= bottom; y++) {
            left += image.getRGB(area.x, y) == color.getRGB() ? 1 : 0;
            rightCount += image.getRGB(right, y) == color.getRGB() ? 1 : 0;
        }
        assertMostly("top", top, area.width);
        assertMostly("bottom", below, area.width);
        assertMostly("left", left, area.height / 2);
        assertMostly("right", rightCount, area.height / 2);
    }

    private void assertMostly(String edge, int count, int length) {
        assertTrue(edge + ": " + count + "/" + length, count >= length * 3 / 4);
    }

    /**
     * @return the area of a text (on a line), as filled by the selection:
     * up to the caret at its end, which is one pixel wide
     */
    private Rectangle area(int start, int end) throws BadLocationException {
        Rectangle2D first = textEditor.modelToView2D(start);
        Rectangle2D last = textEditor.modelToView2D(end);
        return new Rectangle((int) first.getX(), (int) first.getY(),
            (int) (last.getX() - first.getX()) + 1, (int) first.getHeight());
    }
}
