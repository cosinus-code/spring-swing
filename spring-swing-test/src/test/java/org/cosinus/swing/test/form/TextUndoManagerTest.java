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

package org.cosinus.swing.test.form;

import org.cosinus.swing.form.TextUndoManager;
import org.junit.Before;
import org.junit.Test;

import javax.swing.SwingUtilities;
import javax.swing.text.PlainDocument;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The saved state of the text is known after undoing and redoing, as long as it can be reached.
 * <p>
 * Each change is made in its own event on the event dispatch thread, so that it is its own step.
 */
public class TextUndoManagerTest {

    private PlainDocument document;

    private TextUndoManager undoManager;

    @Before
    public void setUp() {
        document = new PlainDocument();
        undoManager = new TextUndoManager();
        document.addUndoableEditListener(undoManager);
        undoManager.markSaved();
    }

    @Test
    public void testSavedStateWithoutSteps() throws Exception {
        insert("one");
        insert("two");
        assertFalse(undoManager.isAtSavedState());

        undoManager.undo();
        undoManager.undo();
        assertEquals("", document.getText(0, document.getLength()));
        assertTrue(undoManager.isAtSavedState());
    }

    /**
     * When the oldest steps are forgotten because of the limit, the saved text without steps cannot be reached.
     */
    @Test
    public void testSavedStateForgottenByTheLimit() throws Exception {
        undoManager.setLimit(2);
        insert("one");
        insert("two");
        insert("three");

        undoManager.undo();
        undoManager.undo();
        assertFalse(undoManager.canUndo());
        assertEquals("one", document.getText(0, document.getLength()));
        assertFalse(undoManager.isAtSavedState());
    }

    @Test
    public void testSavedStepKeptByTheLimit() throws Exception {
        undoManager.setLimit(2);
        insert("one");
        undoManager.markSaved();
        insert("two");
        insert("three");

        undoManager.undo();
        undoManager.undo();
        assertTrue(undoManager.isAtSavedState());
    }

    @Test
    public void testSavedStepForgottenByTheLimit() throws Exception {
        undoManager.setLimit(2);
        insert("one");
        undoManager.markSaved();
        insert("two");
        insert("three");
        insert("four");

        undoManager.undo();
        undoManager.undo();
        assertFalse(undoManager.canUndo());
        assertEquals("onetwo", document.getText(0, document.getLength()));
        assertFalse(undoManager.isAtSavedState());
    }

    private void insert(String text) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            try {
                document.insertString(document.getLength(), text, null);
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        });
    }
}
