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
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import javax.swing.KeyStroke;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static java.awt.event.ActionEvent.ACTION_PERFORMED;
import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_EQUALS;
import static java.awt.event.KeyEvent.VK_MINUS;
import static org.cosinus.swing.form.TextEditor.MAX_FONT_SIZE;
import static org.cosinus.swing.form.TextEditor.MIN_FONT_SIZE;
import static org.junit.Assert.assertEquals;

/**
 * The font size of the text editor is changed with the control key of the platform (Cmd on mac)
 * and equals (plus), minus or 0.
 * <p>
 * The text editor is created in the spring swing context, which injects its ui handler,
 * and its components are initialized as when it is added to a form.
 */
@RunWith(SpringRunner.class)
@SpringSwingBootTest(classes = TestSwingInjectApplication.class)
public class TextEditorFontSizeTest {

    @Rule
    public EventDispatchThreadRule onEventDispatchThread = new EventDispatchThreadRule();

    @Autowired
    private ApplicationUIHandler uiHandler;

    private int modifier;

    private TextEditor textEditor;

    private float initialSize;

    @Before
    public void setUp() {
        modifier = uiHandler.getControlDownKeyMask();
        textEditor = new TextEditor();
        textEditor.setFont(textEditor.getFont().deriveFont(12f));
        textEditor.initComponents();
        initialSize = textEditor.getFontSize();
    }

    @Test
    public void testIncreaseFontSize() {
        press(VK_EQUALS, modifier);
        assertEquals(13, textEditor.getFontSize(), 0);
        press(VK_EQUALS, modifier);
        assertEquals(14, textEditor.getFontSize(), 0);
    }

    @Test
    public void testDecreaseFontSize() {
        press(VK_MINUS, modifier);
        assertEquals(11, textEditor.getFontSize(), 0);
        press(VK_MINUS, modifier);
        assertEquals(10, textEditor.getFontSize(), 0);
    }

    /**
     * The font size is restored to the size it had when the components of the text editor were initialized.
     */
    @Test
    public void testResetFontSize() {
        press(VK_EQUALS, modifier);
        press(VK_EQUALS, modifier);
        press(VK_0, modifier);
        assertEquals(initialSize, textEditor.getFontSize(), 0);

        press(VK_MINUS, modifier);
        press(VK_0, modifier);
        assertEquals(initialSize, textEditor.getFontSize(), 0);
    }

    @Test
    public void testFontSizeIsLimited() {
        for (int index = 0; index < 100; index++) {
            press(VK_EQUALS, modifier);
        }
        assertEquals(MAX_FONT_SIZE, textEditor.getFontSize(), 0);
        for (int index = 0; index < 100; index++) {
            press(VK_MINUS, modifier);
        }
        assertEquals(MIN_FONT_SIZE, textEditor.getFontSize(), 0);
    }

    /**
     * Without the menu shortcut key, the keys are typed in the text.
     */
    @Test
    public void testKeysWithoutModifierDoNotChangeFontSize() {
        press(VK_EQUALS, 0);
        press(VK_MINUS, 0);
        press(VK_0, 0);
        assertEquals(initialSize, textEditor.getFontSize(), 0);
    }

    @Test
    public void testFontKeepsItsFamilyAndStyle() {
        textEditor.setFont(new Font(Font.MONOSPACED, Font.BOLD, 12));
        press(VK_EQUALS, modifier);
        assertEquals(13, textEditor.getFontSize(), 0);
        assertEquals(Font.MONOSPACED, textEditor.getFont().getFamily());
        assertEquals(Font.BOLD, textEditor.getFont().getStyle());
    }

    /**
     * Run the action bound to a key in the text editor, as when it is pressed with the focus in the text editor.
     */
    private void press(int keyCode, int modifiers) {
        ActionListener action = textEditor.getActionForKeyStroke(KeyStroke.getKeyStroke(keyCode, modifiers));
        if (action != null) {
            action.actionPerformed(new ActionEvent(textEditor, ACTION_PERFORMED, null));
        }
    }
}
