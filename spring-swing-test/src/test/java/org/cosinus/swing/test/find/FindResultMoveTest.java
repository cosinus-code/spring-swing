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

import org.cosinus.swing.find.FindResult;
import org.cosinus.swing.find.FindText;
import org.cosinus.swing.find.TextFinder;
import org.cosinus.swing.find.TextFinderByRegularExpression;
import org.cosinus.swing.form.TextEditor;
import org.junit.Before;
import org.junit.Test;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.Highlighter.HighlightPainter;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * The found results, and their highlights, follow the changes of the searched text.
 */
public class FindResultMoveTest {

    private static final HighlightPainter CURRENT_PAINTER = new DefaultHighlightPainter(Color.BLUE);

    private TextEditor textEditor;

    private TextFinder textFinder;

    @Before
    public void setUp() {
        textEditor = new TextEditor() {
            @Override
            public HighlightPainter getCurrentFoundTextHighlightPainter() {
                return CURRENT_PAINTER;
            }
        };
        textEditor.setText("one two one two one");
        textFinder = new TextFinderByRegularExpression(textEditor.getText(), new FindText("one"));
        try (Stream<FindResult> findResults = textFinder.findAll()) {
            findResults.forEach(textEditor::highlightFoundText);
        }
        textEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                try {
                    textFinder.textInserted(event.getOffset(),
                            event.getDocument().getText(event.getOffset(), event.getLength()))
                        .forEach(textEditor::moveFoundTextHighlight);
                } catch (BadLocationException ex) {
                    throw new IllegalStateException(ex);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                textFinder.textRemoved(event.getOffset(), event.getLength())
                    .forEach(textEditor::moveFoundTextHighlight);
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
            }
        });
    }

    @Test
    public void testTextInsertedBeforeMovesTheResult() {
        FindResult result = new FindResult("one", 1, 4, 7);
        assertPositions(6, 9, result.textInserted(0, 2));
        assertPositions(6, 9, result.textInserted(4, 2));
    }

    @Test
    public void testTextInsertedAfterDoesNotChangeTheResult() {
        FindResult result = new FindResult("one", 1, 4, 7);
        assertSame(result, result.textInserted(7, 2));
        assertSame(result, result.textInserted(9, 2));
    }

    @Test
    public void testTextInsertedInsideExtendsTheResult() {
        assertPositions(4, 9, new FindResult("one", 1, 4, 7).textInserted(5, 2));
    }

    @Test
    public void testTextInsertedInEmptyResultMovesIt() {
        assertPositions(6, 6, new FindResult("", 1, 4, 4).textInserted(4, 2));
    }

    @Test
    public void testTextRemoved() {
        FindResult result = new FindResult("one", 1, 4, 7);
        assertPositions(2, 5, result.textRemoved(0, 2));
        assertPositions(2, 3, result.textRemoved(2, 4));
        assertPositions(4, 5, result.textRemoved(5, 2));
        assertPositions(4, 5, result.textRemoved(5, 4));
        assertPositions(2, 2, result.textRemoved(2, 8));
        assertSame(result, result.textRemoved(7, 2));
    }

    @Test
    public void testFinderMovesTheResults() {
        List<FindResult> moved = textFinder.textInserted(9, "XYZ");

        assertEquals(List.of(2, 3), moved.stream().map(FindResult::index).toList());
        assertPositions(8, 14, moved.get(0));
        assertPositions(19, 22, moved.get(1));

        assertPositions(19, 22, findNext(3));
    }

    /**
     * The moved results have the text found at their new positions.
     */
    @Test
    public void testMovedResultsHaveTheirCurrentText() {
        List<FindResult> inserted = textFinder.textInserted(9, "XYZ");
        assertEquals(List.of("oXYZne", "one"), inserted.stream().map(FindResult::text).toList());

        List<FindResult> removed = textFinder.textRemoved(0, 10);
        assertEquals(List.of("", "YZne", "one"), removed.stream().map(FindResult::text).toList());
        assertEquals("YZne two one", textFinder.getInputText());
    }

    @Test
    public void testFinderTextFollowsTheEditedText() throws BadLocationException {
        textEditor.getDocument().insertString(0, ">> ", null);
        textEditor.getDocument().remove(6, 4);
        textEditor.getDocument().insertString(10, "two", null);
        assertEquals(textEditor.getText(), textFinder.getInputText());
    }

    /**
     * The new occurrences are found by refreshing the finder,
     * and the current result stays the same found text.
     */
    @Test
    public void testRefreshFindsTheNewOccurrences() throws BadLocationException {
        textEditor.highlightCurrentFoundText(findNext(2));
        textEditor.getDocument().insertString(0, "one ", null);

        List<FindResult> refreshed = textFinder.refresh();

        assertEquals(List.of(0, 4, 12, 20), refreshed.stream().map(FindResult::startPosition).toList());
        assertEquals(List.of(1, 2, 3, 4), refreshed.stream().map(FindResult::index).toList());
        assertEquals(4, textFinder.count());
        assertEquals(3, textFinder.currentIndex());
        assertPositions(12, 15, textFinder.getCurrentFind());
        assertPositions(20, 23, textFinder.findNext().orElseThrow());
        assertPositions(12, 15, textFinder.findPrevious().orElseThrow());
    }

    /**
     * When the current found text was removed, the current result becomes the next one.
     */
    @Test
    public void testRefreshAfterTheCurrentTextWasRemoved() throws BadLocationException {
        textEditor.highlightCurrentFoundText(findNext(2));
        textEditor.getDocument().remove(8, 3);

        List<FindResult> refreshed = textFinder.refresh();

        assertEquals(List.of(0, 13), refreshed.stream().map(FindResult::startPosition).toList());
        assertEquals(2, textFinder.currentIndex());
        assertPositions(13, 16, textFinder.getCurrentFind());
    }

    @Test
    public void testRefreshAfterTheLastTextWasRemoved() throws BadLocationException {
        textEditor.highlightCurrentFoundText(findNext(3));
        textEditor.getDocument().remove(15, 4);

        assertEquals(2, textFinder.refresh().size());
        assertEquals(2, textFinder.currentIndex());
        assertPositions(8, 11, textFinder.getCurrentFind());
    }

    @Test
    public void testRefreshWithoutCurrentResult() throws BadLocationException {
        textEditor.getDocument().insertString(0, "one ", null);

        assertEquals(4, textFinder.refresh().size());
        assertEquals(0, textFinder.currentIndex());
        assertNull(textFinder.getCurrentFind());
        assertPositions(0, 3, textFinder.findNext().orElseThrow());
    }

    @Test
    public void testRefreshWithoutResults() {
        textFinder.textRemoved(0, textFinder.getInputText().length());
        textFinder.findNext();

        assertEquals(List.of(), textFinder.refresh());
        assertEquals(0, textFinder.count());
        assertNull(textFinder.getCurrentFind());
    }

    /**
     * The found text is the searched text, not its lower case used to find it when not case-sensitive.
     */
    @Test
    public void testFoundTextKeepsItsCase() {
        TextFinder finder = new TextFinderByRegularExpression("One ONE", new FindText("one").caseSensitive(false));
        try (Stream<FindResult> findResults = finder.findAll()) {
            assertEquals(List.of("One", "ONE"), findResults.map(FindResult::text).toList());
        }
        assertEquals(List.of("OxNE"), finder.textInserted(5, "x").stream().map(FindResult::text).toList());
    }

    @Test
    public void testHighlightsFollowTheInsertedText() throws BadLocationException {
        textEditor.getDocument().insertString(0, ">> ", null);
        assertHighlights(3, 6, 11, 14, 19, 22);
        assertHighlightedTexts("one", "one", "one");
    }

    /**
     * A text typed right after a found text is not highlighted.
     */
    @Test
    public void testHighlightsDoNotIncludeTheTextInsertedAtTheirEnd() throws BadLocationException {
        textEditor.getDocument().insertString(11, "s", null);
        assertHighlights(0, 3, 8, 11, 17, 20);
        assertHighlightedTexts("one", "one", "one");
    }

    @Test
    public void testHighlightsFollowTheRemovedText() throws BadLocationException {
        textEditor.getDocument().remove(3, 4);
        assertHighlights(0, 3, 4, 7, 12, 15);
        assertHighlightedTexts("one", "one", "one");
    }

    @Test
    public void testCurrentHighlightFollowsTheTextAndIsRestored() throws BadLocationException {
        textEditor.highlightCurrentFoundText(findNext(2));
        textEditor.getDocument().insertString(0, ">> ", null);
        textEditor.getDocument().insertString(14, "s", null);

        FindResult current = textFinder.getCurrentFind();
        assertPositions(11, 14, current);
        assertEquals(List.of(3, 20), highlightStarts(CURRENT_PAINTER, false));
        assertEquals(List.of(11), highlightStarts(CURRENT_PAINTER, true));

        textEditor.highlightCurrentFoundText(findNext(3));
        assertPositions(20, 23, textFinder.getCurrentFind());
        assertEquals(List.of(20), highlightStarts(CURRENT_PAINTER, true));
        assertHighlights(3, 6, 11, 14, 20, 23);
        assertHighlightedTexts("one", "one", "one");
    }

    private FindResult findNext(int index) {
        FindResult findResult = null;
        while (textFinder.currentIndex() != index) {
            findResult = textFinder.currentIndex() < index ?
                textFinder.findNext().orElseThrow() :
                textFinder.findPrevious().orElseThrow();
        }
        return findResult != null ? findResult : textFinder.getCurrentFind();
    }

    private List<Integer> highlightStarts(HighlightPainter painter, boolean withPainter) {
        return sortedHighlights()
            .filter(highlight -> (highlight.getPainter() == painter) == withPainter)
            .map(Highlight::getStartOffset)
            .toList();
    }

    private Stream<Highlight> sortedHighlights() {
        return Arrays.stream(textEditor.getHighlighter().getHighlights())
            .sorted((first, second) -> Integer.compare(first.getStartOffset(), second.getStartOffset()));
    }

    private void assertHighlights(int... positions) {
        assertEquals(Arrays.stream(positions).boxed().toList(),
            sortedHighlights()
                .flatMap(highlight -> Stream.of(highlight.getStartOffset(), highlight.getEndOffset()))
                .toList());
    }

    private void assertHighlightedTexts(String... texts) {
        assertEquals(List.of(texts), sortedHighlights()
            .map(highlight -> {
                try {
                    return textEditor.getText(highlight.getStartOffset(),
                        highlight.getEndOffset() - highlight.getStartOffset());
                } catch (BadLocationException ex) {
                    throw new IllegalStateException(ex);
                }
            })
            .toList());
    }

    private void assertPositions(int start, int end, FindResult findResult) {
        assertEquals("start", start, findResult.startPosition());
        assertEquals("end", end, findResult.endPosition());
    }
}
