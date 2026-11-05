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

import org.cosinus.swing.form.TextEditor;
import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.cosinus.swing.test.boot.inject.TestSwingInjectApplication;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.awt.event.ActionEvent.ACTION_PERFORMED;
import static java.awt.event.KeyEvent.VK_Y;
import static java.awt.event.KeyEvent.VK_Z;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * The changes of the text are undone and redone in steps, as in a text editor.
 * <p>
 * Each action (e.g. a typed character) is run in its own event on the event dispatch thread, as when made by the user,
 * because the changes made by the same event are one step.
 */
@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = TestSwingInjectApplication.class)
public class TextEditorUndoTest {

    @Autowired
    private ApplicationUIHandler uiHandler;

    private TextEditor textEditor;

    @Before
    public void setUp() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            textEditor = new TextEditor();
            textEditor.initComponents();
            textEditor.initComponent();
        });
    }

    @Test
    public void testTypedWordsAreUndoneOneByOne() throws Exception {
        type("hello world");
        assertText("hello world");

        undo();
        assertText("hello ");
        undo();
        assertText("");
        assertFalse(getOnEdt(textEditor::canUndo));

        redo();
        assertText("hello ");
        redo();
        assertText("hello world");
        assertFalse(getOnEdt(textEditor::canRedo));
    }

    @Test
    public void testRemovedCharactersAreOneStep() throws Exception {
        load("hello world");
        onEdt(() -> textEditor.setCaretPosition(11));
        for (int index = 0; index < 5; index++) {
            run(DefaultEditorKit.deletePrevCharAction);
        }
        assertText("hello ");
        onEdt(() -> textEditor.setCaretPosition(0));
        for (int index = 0; index < 2; index++) {
            run(DefaultEditorKit.deleteNextCharAction);
        }
        assertText("llo ");

        undo();
        assertText("hello ");
        undo();
        assertText("hello world");
    }

    @Test
    public void testReplacedSelectionIsOneStep() throws Exception {
        load("one two");
        onEdt(() -> textEditor.select(4, 7));
        type("x");
        assertText("one x");

        undo();
        assertText("one two");
    }

    @Test
    public void testSetTextIsOneStep() throws Exception {
        load("one");
        onEdt(() -> textEditor.setText("two"));

        undo();
        assertText("one");
        redo();
        assertText("two");
    }

    /**
     * A text typed after a pasted text is another step.
     */
    @Test
    public void testPastedTextIsOneStep() throws Exception {
        onEdt(() -> textEditor.replaceSelection("pasted"));
        type("x");

        undo();
        assertText("pasted");
        undo();
        assertText("");
    }

    @Test
    public void testLoadedTextCannotBeUndone() throws Exception {
        type("before");
        load("loaded");

        assertFalse(getOnEdt(textEditor::canUndo));
        undo();
        assertText("loaded");
    }

    @Test
    public void testNewChangeForgetsUndoneChanges() throws Exception {
        type("one two");
        undo();
        type("three");
        assertText("one three");

        assertFalse(getOnEdt(textEditor::canRedo));
    }

    @Test
    public void testNotEditableTextCannotBeUndone() throws Exception {
        type("text");
        onEdt(() -> textEditor.setEditable(false));

        assertFalse(getOnEdt(textEditor::canUndo));
        undo();
        assertText("text");
    }

    @Test
    public void testUndoAndRedoKeys() throws Exception {
        int modifier = uiHandler.getControlDownKeyMask();
        type("one two");

        press(VK_Z, modifier);
        assertText("one ");
        press(VK_Y, modifier);
        assertText("one two");
        assertTrue(getOnEdt(textEditor::canUndo));
    }

    /**
     * The text is dirty when it differs from the saved text, after undoing or redoing changes.
     */
    @Test
    public void testDirtyFollowsTheUndoHistory() throws Exception {
        type("one two");
        save();
        assertDirty(false);
        type(" three");
        assertDirty(true);

        // the space typed after the saved word is a step, as the next word
        undo();
        assertText("one two ");
        assertDirty(true);
        undo();
        assertText("one two");
        assertDirty(false);
        undo();
        assertText("one ");
        assertDirty(true);
        redo();
        assertText("one two");
        assertDirty(false);
        redo();
        redo();
        assertText("one two three");
        assertDirty(true);
    }

    @Test
    public void testLoadedTextIsSaved() throws Exception {
        type("before");
        load("loaded");
        assertDirty(false);
        type(" text");
        assertDirty(true);

        undo();
        assertText("loaded ");
        assertDirty(true);
        undo();
        assertText("loaded");
        assertDirty(false);
    }

    /**
     * A word typed on, after it was saved, is a new change, undone to the saved text.
     */
    @Test
    public void testSavedWhileTyping() throws Exception {
        type("one");
        save();
        type("s");
        assertDirty(true);

        undo();
        assertText("one");
        assertDirty(false);
    }

    /**
     * When the changes leading back to the saved text are replaced by a new change, the text stays dirty.
     */
    @Test
    public void testSavedTextCannotBeReachedAfterNewChange() throws Exception {
        type("one two");
        save();
        undo();
        type("x");
        assertText("one x");

        undo();
        assertText("one ");
        assertDirty(true);
        redo();
        assertDirty(true);
    }

    /**
     * When the changes made after the saved text are discarded (e.g. with the editor reset),
     * the text is dirty or saved as it was.
     */
    @Test
    public void testDiscardedChangesKeepTheDirtyState() throws Exception {
        type("one");
        save();
        type(" two");
        onEdt(textEditor::discardUndoableEdits);
        assertDirty(true);
        type("s");
        undo();
        assertText("one two");
        assertDirty(true);
    }

    private void save() throws Exception {
        onEdt(() -> textEditor.setDirty(false));
    }

    private void assertDirty(boolean dirty) throws Exception {
        assertEquals(dirty, getOnEdt(textEditor::isDirty));
    }

    private void type(String text) throws Exception {
        for (char character : text.toCharArray()) {
            onEdt(() -> textEditor.replaceSelection(String.valueOf(character)));
        }
    }

    private void load(String text) throws Exception {
        onEdt(() -> {
            textEditor.setLoading(true);
            textEditor.setText(text);
            textEditor.setLoading(false);
        });
    }

    private void undo() throws Exception {
        onEdt(textEditor::undo);
    }

    private void redo() throws Exception {
        onEdt(textEditor::redo);
    }

    private void run(String actionName) throws Exception {
        onEdt(() -> textEditor.getActionMap().get(actionName)
            .actionPerformed(new ActionEvent(textEditor, ACTION_PERFORMED, null)));
    }

    /**
     * Run the action bound to a key in the text editor, as when it is pressed with the focus in the text editor.
     */
    private void press(int keyCode, int modifiers) throws Exception {
        onEdt(() -> {
            ActionListener action = textEditor.getActionForKeyStroke(KeyStroke.getKeyStroke(keyCode, modifiers));
            action.actionPerformed(new ActionEvent(textEditor, ACTION_PERFORMED, null));
        });
    }

    private void assertText(String expected) throws Exception {
        assertEquals(expected, getOnEdt(textEditor::getText));
    }

    private void onEdt(Runnable runnable) throws Exception {
        SwingUtilities.invokeAndWait(runnable);
    }

    private <T> T getOnEdt(Supplier<T> supplier) throws Exception {
        AtomicReference<T> result = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> result.set(supplier.get()));
        return result.get();
    }
}
