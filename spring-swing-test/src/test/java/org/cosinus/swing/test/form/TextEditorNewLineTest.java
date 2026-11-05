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
import org.cosinus.swing.test.EventDispatchThreadRule;
import org.cosinus.swing.test.boot.SpringSwingBootTest;
import org.cosinus.swing.test.boot.inject.TestSwingInjectApplication;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;

import static java.awt.event.ActionEvent.ACTION_PERFORMED;
import static java.awt.event.KeyEvent.VK_ENTER;
import static org.junit.Assert.assertEquals;

/**
 * A new line added with Enter has the indentation of the line where it is added.
 */
@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = TestSwingInjectApplication.class)
public class TextEditorNewLineTest {

    @Rule
    public EventDispatchThreadRule onEventDispatchThread = new EventDispatchThreadRule();

    private TextEditor textEditor;

    @Before
    public void setUp() {
        textEditor = new TextEditor();
        textEditor.initComponents();
    }

    @Test
    public void testNewLineKeepsTheIndentation() {
        textEditor.setText("first\n    second");
        textEditor.setCaretPosition(textEditor.getText().length());

        pressEnter();

        assertEquals("first\n    second\n    ", textEditor.getText());
        assertEquals(textEditor.getText().length(), textEditor.getCaretPosition());
    }

    @Test
    public void testNewLineKeepsTabsAndSpaces() {
        textEditor.setText("\t  one");
        textEditor.setCaretPosition(textEditor.getText().length());

        pressEnter();

        assertEquals("\t  one\n\t  ", textEditor.getText());
    }

    @Test
    public void testNewLineWithoutIndentation() {
        textEditor.setText("one");
        textEditor.setCaretPosition(3);

        pressEnter();

        assertEquals("one\n", textEditor.getText());
    }

    /**
     * A line split in the middle continues indented, with the rest of the line.
     */
    @Test
    public void testNewLineInsideTheLine() {
        textEditor.setText("  one two");
        textEditor.setCaretPosition(6);

        pressEnter();

        assertEquals("  one \n  two", textEditor.getText());
        assertEquals(9, textEditor.getCaretPosition());
    }

    /**
     * Inside the indentation, only the indentation before the caret is added.
     */
    @Test
    public void testNewLineInsideTheIndentation() {
        textEditor.setText("    one");
        textEditor.setCaretPosition(2);

        pressEnter();

        assertEquals("  \n    one", textEditor.getText());
    }

    @Test
    public void testNewLineReplacesTheSelection() {
        textEditor.setText("  ab cd");
        textEditor.select(3, 6);

        pressEnter();

        assertEquals("  a\n  d", textEditor.getText());
    }

    @Test
    public void testNewLineOnAnEmptyLine() {
        textEditor.setText("  one\n");
        textEditor.setCaretPosition(6);

        pressEnter();

        assertEquals("  one\n\n", textEditor.getText());
    }

    @Test
    public void testNewLineInNotEditableText() {
        textEditor.setText("  one");
        textEditor.setEditable(false);
        textEditor.setCaretPosition(5);

        pressEnter();

        assertEquals("  one", textEditor.getText());
    }

    private void pressEnter() {
        textEditor.getActionForKeyStroke(KeyStroke.getKeyStroke(VK_ENTER, 0))
            .actionPerformed(new ActionEvent(textEditor, ACTION_PERFORMED, null));
    }
}
