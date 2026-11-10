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

import lombok.Getter;
import org.cosinus.swing.error.SpringSwingException;
import org.cosinus.swing.find.FindResult;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter.Highlight;
import javax.swing.text.Highlighter.HighlightPainter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

import static java.awt.Color.BLUE;
import static java.awt.Color.YELLOW;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_EQUALS;
import static java.awt.event.KeyEvent.VK_MINUS;
import static java.awt.event.KeyEvent.VK_Y;
import static java.awt.event.KeyEvent.VK_Z;
import static javax.swing.KeyStroke.getKeyStroke;
import static java.lang.Math.clamp;
import static java.util.Optional.ofNullable;
import static javax.swing.text.DefaultEditorKit.insertBreakAction;
import static javax.swing.text.PlainDocument.tabSizeAttribute;
import static org.cosinus.swing.color.SystemColor.TEXT_PANE_SELECTION_BACKGROUND;

public class TextEditor extends EditorPane implements DocumentListener, FormComponent, TextComponent {

    public static final int DEFAULT_TAB_SIZE = 4;

    public static final Color DEFAULT_FOUND_TEXT_HIGHLIGHT_COLOR = YELLOW;

    public static final Color DEFAULT_CURRENT_FOUND_TEXT_HIGHLIGHT_COLOR = BLUE;

    public static final float MIN_FONT_SIZE = 8;

    public static final float MAX_FONT_SIZE = 30;

    public static final float FONT_SIZE_STEP = 1;

    private static final String INCREASE_FONT_SIZE_ACTION = "increase-font-size";

    private static final String DECREASE_FONT_SIZE_ACTION = "decrease-font-size";

    private static final String RESET_FONT_SIZE_ACTION = "reset-font-size";

    private static final String UNDO_ACTION = "undo";

    private static final String REDO_ACTION = "redo";

    @Autowired
    protected ApplicationUIHandler uiHandler;

    private HighlightPainter foundTextHighlightPainter;

    private HighlightPainter currentFoundTextHighlightPainter;

    private Highlight currentFoundTextHighlight;

    private int currentFoundTextIndex;

    private final Map<Integer, Highlight> foundTextHighlightsMap;

    @Getter
    private boolean dirty;

    @Getter
    private boolean loading;

    private final TextUndoManager undoManager;

    /**
     * Records the changes of the text in the undo manager, except the text loaded.
     */
    private final UndoableEditListener undoableEditListener;

    /**
     * The font size before it was first changed, restored by {@link #resetFontSize()}.
     */
    private float initialFontSize;

    public TextEditor() {
        foundTextHighlightsMap = new HashMap<>();
        undoManager = new TextUndoManager();
        undoableEditListener = event -> {
            if (!isLoading()) {
                undoManager.undoableEditHappened(event);
            }
        };
    }

    @Override
    public void initComponents() {
        initUndoKeys();
        initNewLineIndentation();
        initialFontSize = getFontSize();

        ofNullable(getDocument()).ifPresent(document -> document.addUndoableEditListener(undoableEditListener));
        addPropertyChangeListener("document", event -> {
            if (event.getOldValue() instanceof Document oldDocument) {
                oldDocument.removeUndoableEditListener(undoableEditListener);
            }
            if (event.getNewValue() instanceof Document newDocument) {
                newDocument.addUndoableEditListener(undoableEditListener);
            }
            undoManager.discardAllEdits();
        });
    }

    /**
     * Replace the action adding a new line (bound to Enter) with one keeping the indentation of the line.
     */
    private void initNewLineIndentation() {
        getActionMap().put(insertBreakAction, action(this::insertNewLine));
    }

    /**
     * Add a new line instead of the selection, indented as the line where it is added:
     * the new line starts with the spaces and tabs which start that line, up to the new line.
     */
    public void insertNewLine() {
        if (isEditable() && isEnabled()) {
            replaceSelection("\n" + getIndentation(getSelectionStart()));
        }
    }

    /**
     * Get the indentation of the line at a position: the spaces and tabs which start the line, up to the position.
     *
     * @param position the position in the text
     * @return the indentation
     */
    public String getIndentation(int position) {
        Document document = getDocument();
        Element line = document.getDefaultRootElement()
            .getElement(document.getDefaultRootElement().getElementIndex(position));
        try {
            String start = document.getText(line.getStartOffset(), position - line.getStartOffset());
            int length = 0;
            while (length < start.length() && (start.charAt(length) == ' ' || start.charAt(length) == '\t')) {
                length++;
            }
            return start.substring(0, length);
        } catch (BadLocationException ex) {
            throw new SpringSwingException(ex);
        }
    }

    /**
     * Bind the keys undoing and redoing the changes of the text, with the control key of the platform (Cmd on mac):
     * Z to undo, Y or shift Z to redo.
     */
    private void initUndoKeys() {
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        inputMap.put(uiHandler.getControlDownKeyStroke(VK_Z), UNDO_ACTION);
        inputMap.put(uiHandler.getControlDownKeyStroke(VK_Y), REDO_ACTION);

        getActionMap().put(UNDO_ACTION, action(this::undo));
        getActionMap().put(REDO_ACTION, action(this::redo));
    }

    /**
     * Mark the text as changed, or as saved (when it is saved or loaded):
     * undoing or redoing the changes makes it dirty again, until they return to the saved text.
     *
     * @param dirty true if the text is changed, false if it is saved
     */
    public void setDirty(boolean dirty) {
        this.dirty = dirty;
        if (!dirty) {
            undoManager.markSaved();
        }
    }

    /**
     * Start or finish loading a text, which cannot be undone.
     * When the loading is finished, the loaded text is the saved text (not dirty).
     *
     * @param loading true when the loading starts, false when it is finished
     */
    public void setLoading(boolean loading) {
        this.loading = loading;
        undoManager.discardAllEdits();
        if (!loading) {
            setDirty(false);
        }
    }

    public boolean canUndo() {
        return isEditable() && undoManager.canUndo();
    }

    public boolean canRedo() {
        return isEditable() && undoManager.canRedo();
    }

    /**
     * Undo the last change of the text (see {@link TextUndoManager} for what a change is), if any.
     */
    public void undo() {
        if (canUndo()) {
            undoManager.undo();
            setDirty(!undoManager.isAtSavedState());
        }
    }

    /**
     * Redo the last undone change of the text, if any.
     */
    public void redo() {
        if (canRedo()) {
            undoManager.redo();
            setDirty(!undoManager.isAtSavedState());
        }
    }

    /**
     * Forget the changes of the text, which can no longer be undone.
     */
    public void discardUndoableEdits() {
        undoManager.discardAllEdits();
    }

    /**
     * Bind the keys changing the font size, with the menu shortcut key (Ctrl, or Cmd on mac):
     * plus (also as shift equals, and on the numeric keypad) to increase it,
     * minus (also on the numeric keypad) to decrease it, and 0 to restore it.
     */
    public void initFontSizeKeys() {
        InputMap inputMap = getInputMap(WHEN_FOCUSED);
        inputMap.put(uiHandler.getControlDownKeyStroke(VK_EQUALS), INCREASE_FONT_SIZE_ACTION);
        inputMap.put(uiHandler.getControlDownKeyStroke(VK_MINUS), DECREASE_FONT_SIZE_ACTION);
        inputMap.put(uiHandler.getControlDownKeyStroke(VK_0), RESET_FONT_SIZE_ACTION);

        getActionMap().put(INCREASE_FONT_SIZE_ACTION, action(this::increaseFontSize));
        getActionMap().put(DECREASE_FONT_SIZE_ACTION, action(this::decreaseFontSize));
        getActionMap().put(RESET_FONT_SIZE_ACTION, action(this::resetFontSize));
    }

    private static AbstractAction action(Runnable runnable) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                runnable.run();
            }
        };
    }

    public float getFontSize() {
        return getFont().getSize2D();
    }

    /**
     * Set the font size, limited to {@value #MIN_FONT_SIZE} - {@value #MAX_FONT_SIZE}.
     *
     * @param size the font size
     */
    public void setFontSize(float size) {
        setFont(getFont().deriveFont(clamp(size, MIN_FONT_SIZE, MAX_FONT_SIZE)));
    }

    public void increaseFontSize() {
        setFontSize(getFontSize() + FONT_SIZE_STEP);
    }

    public void decreaseFontSize() {
        setFontSize(getFontSize() - FONT_SIZE_STEP);
    }

    /**
     * Restore the font size from before it was first changed.
     */
    public void resetFontSize() {
        setFontSize(initialFontSize);
    }

    public void initComponent() {
        getDocument().addDocumentListener(this);
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
        undoManager.discardAllEdits();
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
            foundTextHighlightPainter = new SelectionAwareHighlightPainter(DEFAULT_FOUND_TEXT_HIGHLIGHT_COLOR);
        }
        return foundTextHighlightPainter;
    }

    public HighlightPainter getCurrentFoundTextHighlightPainter() {
        if (currentFoundTextHighlightPainter == null) {
            Color highlightColor = ofNullable(uiHandler.getColor(TEXT_PANE_SELECTION_BACKGROUND))
                .orElse(DEFAULT_CURRENT_FOUND_TEXT_HIGHLIGHT_COLOR);
            currentFoundTextHighlightPainter = new SelectionAwareHighlightPainter(highlightColor);
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
            highlightFoundText(findResult.index(), findResult.startPosition(), findResult.endPosition());
        }
    }

    private void highlightFoundText(int index, int startPosition, int endPosition) {
        Highlight highLight = highlightText(startPosition, endPosition, getFoundTextHighlightPainter());
        foundTextHighlightsMap.put(index, highLight);
    }

    public void highlightCurrentFoundText(FindResult findResult) {
        highlightCurrentFoundText(findResult, true);
    }

    /**
     * Highlight the current found text.
     *
     * @param findResult the current found result
     * @param moveCaret  true to move the caret at the end of the found text,
     *                   false to keep it (e.g. when the text is being edited)
     */
    public void highlightCurrentFoundText(FindResult findResult, boolean moveCaret) {
        if (currentFoundTextHighlight != null) {
            getHighlighter().removeHighlight(currentFoundTextHighlight);
            highlightFoundText(
                currentFoundTextIndex,
                currentFoundTextHighlight.getStartOffset(),
                currentFoundTextHighlight.getEndOffset());
        }
        ofNullable(foundTextHighlightsMap.remove(findResult.index()))
            .ifPresent(getHighlighter()::removeHighlight);

        currentFoundTextHighlight = highlightText(
            findResult.startPosition(), findResult.endPosition(), getCurrentFoundTextHighlightPainter());
        currentFoundTextIndex = findResult.index();
        if (moveCaret) {
            setCaretPosition(findResult.endPosition());
        }
    }

    /**
     * Move the highlight of a found text to the positions of the found result,
     * after the found result was moved by a change of the text (see {@link FindResult#textInserted(int, int)}).
     * <p>
     * The highlights follow the changes of the text by themselves,
     * but they would include a text inserted at their end, which is not part of the found result.
     *
     * @param findResult the moved found result
     */
    public void moveFoundTextHighlight(FindResult findResult) {
        Highlight highlight = currentFoundTextHighlight != null && currentFoundTextIndex == findResult.index() ?
            currentFoundTextHighlight :
            foundTextHighlightsMap.get(findResult.index());
        if (highlight != null &&
            (highlight.getStartOffset() != findResult.startPosition() ||
                highlight.getEndOffset() != findResult.endPosition())) {
            try {
                getHighlighter().changeHighlight(highlight, findResult.startPosition(), findResult.endPosition());
            } catch (BadLocationException ex) {
                throw new SpringSwingException(ex);
            }
        }
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
        currentFoundTextHighlight = null;
    }

    public void preventCancelAction() {
    }
}
