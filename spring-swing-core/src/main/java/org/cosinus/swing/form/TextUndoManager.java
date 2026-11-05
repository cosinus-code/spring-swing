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

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentEvent.EventType;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;
import java.awt.*;

import static javax.swing.event.DocumentEvent.EventType.INSERT;
import static javax.swing.event.DocumentEvent.EventType.REMOVE;

/**
 * Undo manager of a text, which undoes and redoes the changes of the text in steps, as a text editor does:
 * <ul>
 *     <li>the changes made by the same action (e.g. a paste, a replaced selection, a text set) are one step,</li>
 *     <li>the characters typed one after the other are one step per word (with the spaces after it),</li>
 *     <li>the characters removed one after the other (with backspace or delete) are one step.</li>
 * </ul>
 * <p>
 * It also remembers the saved state of the text (see {@link #markSaved()}),
 * to know if undoing or redoing changes returns to it (see {@link #isAtSavedState()}).
 */
public class TextUndoManager implements UndoableEditListener {

    public static final int DEFAULT_UNDO_LIMIT = 1000;

    private final StepUndoManager undoManager;

    /**
     * The last step done when the text was saved, or null if no step was done.
     */
    private UndoableEdit savedStep;

    /**
     * True if the saved state cannot be reached anymore by undoing or redoing steps.
     */
    private boolean savedStateLost;

    private CompoundEdit step;

    private AWTEvent stepEvent;

    private EventType stepType;

    private int stepOffset;

    private int stepEnd;

    private boolean stepEndsWithSpace;

    /**
     * True if the current step has only characters typed or removed one by one, so it can be continued.
     */
    private boolean stepTyping;

    public TextUndoManager() {
        undoManager = new StepUndoManager();
        undoManager.setLimit(DEFAULT_UNDO_LIMIT);
    }

    @Override
    public void undoableEditHappened(UndoableEditEvent event) {
        addEdit(event.getEdit(), event.getSource() instanceof Document document ? document : null);
    }

    private void addEdit(UndoableEdit edit, Document document) {
        AWTEvent currentEvent = EventQueue.getCurrentEvent();
        DocumentEvent change = edit instanceof DocumentEvent documentEvent ? documentEvent : null;
        if (step == null || !continuesStep(change, currentEvent, document)) {
            endStep();
            step = new CompoundEdit();
            undoManager.addStep(step);
            stepType = change != null ? change.getType() : null;
            stepOffset = change != null ? change.getOffset() : -1;
            stepTyping = change != null && change.getLength() == 1;
        } else if (currentEvent != null && currentEvent == stepEvent) {
            stepTyping = false;
        }
        step.addEdit(edit);
        stepEvent = currentEvent;
        if (change != null) {
            stepEnd = change.getOffset() + (change.getType() == INSERT ? change.getLength() : 0);
            stepOffset = change.getOffset();
            stepEndsWithSpace = change.getType() == INSERT && endsWithSpace(document, change);
        }
    }

    /**
     * @return true if the change continues the current step:
     * it is made by the same action, or it continues the typing or the removing of characters
     */
    private boolean continuesStep(DocumentEvent change, AWTEvent currentEvent, Document document) {
        if (currentEvent != null && currentEvent == stepEvent) {
            return true;
        }
        if (!stepTyping || change == null || change.getType() != stepType || change.getLength() != 1) {
            return false;
        }
        if (change.getType() == INSERT) {
            // a new word starts a new step
            return change.getOffset() == stepEnd && !(stepEndsWithSpace && !endsWithSpace(document, change));
        }
        if (change.getType() == REMOVE) {
            // backspace removes before the previous removed character, delete removes at the same offset
            return change.getOffset() == stepOffset - 1 || change.getOffset() == stepOffset;
        }
        return false;
    }

    private boolean endsWithSpace(Document document, DocumentEvent change) {
        if (document == null || change.getLength() == 0) {
            return false;
        }
        try {
            String text = document.getText(change.getOffset() + change.getLength() - 1, 1);
            return Character.isWhitespace(text.charAt(0));
        } catch (BadLocationException ex) {
            return false;
        }
    }

    /**
     * End the current step, so that the next change starts a new one.
     */
    public void endStep() {
        if (step != null) {
            step.end();
            step = null;
            stepEvent = null;
        }
    }

    public boolean canUndo() {
        endStep();
        return undoManager.canUndo();
    }

    public boolean canRedo() {
        endStep();
        return undoManager.canRedo();
    }

    /**
     * Undo the last step, if any.
     */
    public void undo() {
        if (canUndo()) {
            undoManager.undo();
        }
    }

    /**
     * Redo the last undone step, if any.
     */
    public void redo() {
        if (canRedo()) {
            undoManager.redo();
        }
    }

    /**
     * Forget all steps (e.g. when a new text is loaded).
     * The current text stays saved if it was.
     */
    public void discardAllEdits() {
        endStep();
        savedStateLost = !isAtSavedState();
        savedStep = null;
        undoManager.discardAllEdits();
    }

    /**
     * Remember the current text as saved, so that a next change is a new step.
     */
    public void markSaved() {
        endStep();
        savedStep = undoManager.lastDoneStep();
        savedStateLost = false;
    }

    /**
     * @return true if the text is the saved text, after undoing or redoing steps
     */
    public boolean isAtSavedState() {
        return !savedStateLost && undoManager.lastDoneStep() == savedStep;
    }

    /**
     * Undo manager which gives the last step done, and tells when the oldest steps done are forgotten.
     */
    private class StepUndoManager extends UndoManager {

        /**
         * True while the undone steps are trimmed, to add a new step instead.
         */
        private boolean trimmingUndoneSteps;

        UndoableEdit lastDoneStep() {
            return editToBeUndone();
        }

        /**
         * Add a new step: the undone steps, if any, are trimmed first
         * (the saved step, if among them, is trimmed with them).
         */
        void addStep(UndoableEdit step) {
            trimmingUndoneSteps = true;
            try {
                addEdit(step);
            } finally {
                trimmingUndoneSteps = false;
            }
        }

        /**
         * The steps are trimmed when undone steps are replaced by a new step (first, when a step is added),
         * and from the oldest one when the limit is reached.
         */
        @Override
        protected void trimEdits(int from, int to) {
            if (trimmingUndoneSteps) {
                trimmingUndoneSteps = false;
            } else if (from == 0 && from <= to) {
                trimOldestSteps(to);
            }
            super.trimEdits(from, to);
        }

        /**
         * The oldest steps, up to an index, are forgotten:
         * the saved text is reached without steps done if the saved step is the last forgotten one,
         * and cannot be reached anymore if it was before (or if it was the text without steps done).
         */
        private void trimOldestSteps(int to) {
            int savedIndex = savedStep != null ? edits.indexOf(savedStep) : -1;
            if (savedStep == null || (savedIndex >= 0 && savedIndex < to)) {
                savedStateLost = true;
            } else if (savedIndex == to) {
                savedStep = null;
            }
        }
    }

    public void setLimit(int limit) {
        undoManager.setLimit(limit);
    }
}
