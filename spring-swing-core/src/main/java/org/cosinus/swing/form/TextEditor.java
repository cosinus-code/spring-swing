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

import org.cosinus.swing.error.SpringSwingException;
import org.cosinus.swing.find.FindResult;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.Highlighter.HighlightPainter;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;

import static java.awt.Color.BLUE;
import static java.awt.Color.YELLOW;
import static java.awt.event.KeyEvent.VK_BACK_SPACE;
import static java.awt.event.KeyEvent.VK_DELETE;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_TAB;
import static java.util.Optional.ofNullable;
import static javax.swing.text.PlainDocument.tabSizeAttribute;
import static org.cosinus.swing.color.SystemColor.TEXT_PANE_SELECTION_BACKGROUND;

public class TextEditor extends EditorPane implements DocumentListener, FormComponent {

    public static final int DEFAULT_TAB_SIZE = 4;

    public static final Color DEFAULT_FOUND_TEXT_HIGHLIGHT_COLOR = YELLOW;

    public static final Color DEFAULT_CURRENT_FOUND_TEXT_HIGHLIGHT_COLOR = BLUE;

    @Autowired
    private ApplicationUIHandler uiHandler;

    private HighlightPainter foundTextHighlightPainter;

    private HighlightPainter currentFoundTextHighlightPainter;

    private Highlight currentFoundTextHighlight;

    private final Map<Integer, Highlight> foundTextHighlightsMap;

    private boolean dirty;

    private boolean loading;

    public TextEditor() {
        foundTextHighlightsMap = new HashMap<>();
    }

    public void initComponent() {
        getDocument().addDocumentListener(this);
    }

    public boolean isEditorKey(KeyEvent keyEvent) {
        return keyEvent.getKeyCode() == VK_ENTER
            || keyEvent.getKeyCode() == VK_TAB
            || keyEvent.getKeyCode() == VK_DELETE
            || keyEvent.getKeyCode() == VK_BACK_SPACE;
    }

    public String getLineAtIndex(int index) {
        try {
            Element element = getElementAtIndex(index);
            int start = element.getStartOffset();
            int end = index == element.getElementCount() - 1 ? element.getEndOffset() - 1 : element.getEndOffset();
            return getText(start, end - start);
        } catch (BadLocationException ex) {
            throw new SpringSwingException(ex);
        }
    }

    private Element getElementAtIndex(int index) throws BadLocationException {
        if (index < 0) {
            throw new BadLocationException("Negative line", -1);
        }

        return ofNullable(getDocument())
            .map(Document::getDefaultRootElement)
            .filter(root -> index < root.getElementCount())
            .map(root -> root.getElement(index))
            .orElseThrow(() -> new BadLocationException("No such line", getDocument().getLength() + 1));
    }

    public int getLineCount() {
        return ofNullable(getDocument())
            .map(Document::getDefaultRootElement)
            .map(Element::getElementCount)
            .orElse(0);
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public void append(String str) {
        ofNullable(getDocument())
            .ifPresent(doc -> {
                try {
                    doc.insertString(doc.getLength(), str, null);
                } catch (BadLocationException ex) {
                    throw new SpringSwingException(ex);
                }
            });
    }

    public void reset() {
        setText("");
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        markAsDirtyIfNotLoading();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        markAsDirtyIfNotLoading();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        markAsDirtyIfNotLoading();
    }

    private void markAsDirtyIfNotLoading() {
        if (!isLoading()) {
            setDirty(true);
        }
    }

    public int getTabSize() {
        return ofNullable(getDocument())
            .map(doc -> (Integer) doc.getProperty(tabSizeAttribute))
            .orElse(DEFAULT_TAB_SIZE);
    }

    public void setTabSize(int size) {
        ofNullable(getDocument())
            .ifPresent(doc -> {
                int old = getTabSize();
                doc.putProperty(tabSizeAttribute, size);
                firePropertyChange(tabSizeAttribute, old, size);
            });
    }

    public HighlightPainter getFoundTextHighlightPainter() {
        if (foundTextHighlightPainter == null) {
            foundTextHighlightPainter = new DefaultHighlightPainter(DEFAULT_FOUND_TEXT_HIGHLIGHT_COLOR);
        }
        return foundTextHighlightPainter;
    }

    public HighlightPainter getCurrentFoundTextHighlightPainter() {
        if (currentFoundTextHighlightPainter == null) {
            Color highlightColor = ofNullable(uiHandler.getColor(TEXT_PANE_SELECTION_BACKGROUND))
                .orElse(DEFAULT_CURRENT_FOUND_TEXT_HIGHLIGHT_COLOR);
            currentFoundTextHighlightPainter = new DefaultHighlightPainter(highlightColor);
        }
        return currentFoundTextHighlightPainter;
    }

    public void selectFoundText(FindResult findResult) {
        if (findResult != null) {
            setCaretPosition(findResult.startPosition());
            moveCaretPosition(findResult.endPosition());
        }
    }

    public void highlightFoundText(FindResult findResult) {
        if (findResult != null) {
            highlightFoundText(findResult.startPosition(), findResult.endPosition());
        }
    }

    private void highlightFoundText(int startPosition, int endPosition) {
        Highlight highLight = highlightText(startPosition, endPosition, getFoundTextHighlightPainter());
        foundTextHighlightsMap.put(startPosition, highLight);
    }

    public void highlightCurrentFoundText(FindResult findResult) {
        ofNullable(foundTextHighlightsMap.get(findResult.startPosition()))
            .ifPresent(getHighlighter()::removeHighlight);

        if (currentFoundTextHighlight != null) {
            getHighlighter().removeHighlight(currentFoundTextHighlight);
            if (foundTextHighlightsMap.get(currentFoundTextHighlight.getStartOffset()) != null) {
                highlightFoundText(
                    currentFoundTextHighlight.getStartOffset(),
                    currentFoundTextHighlight.getEndOffset());
            }
        }
        currentFoundTextHighlight = highlightText(
            findResult.startPosition(), findResult.endPosition(), getCurrentFoundTextHighlightPainter());
        setCaretPosition(findResult.endPosition());
    }

    public Highlight highlightText(int start, int end, HighlightPainter highlighter) {
        try {
            return (Highlight) getHighlighter().addHighlight(start, end, highlighter);
        } catch (BadLocationException ex) {
            throw new SpringSwingException(ex);
        }
    }

    public void removeAllHighlights() {
        getHighlighter().removeAllHighlights();
        foundTextHighlightsMap.clear();
    }

    public void preventCancelAction() {
    }
}
