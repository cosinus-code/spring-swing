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

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.Optional;

import static java.awt.event.KeyEvent.*;
import static java.util.Optional.*;

public interface TextComponent {

    default boolean isHexaChar(int character) {
        return character >= '0' && character <= '9'
            || character >= 'A' && character <= 'F'
            || character >= 'a' && character <= 'f';
    }

    default boolean isMovementKey(KeyEvent keyEvent) {
        return keyEvent.getKeyCode() == KeyEvent.VK_LEFT
            || keyEvent.getKeyCode() == KeyEvent.VK_RIGHT
            || keyEvent.getKeyCode() == KeyEvent.VK_UP
            || keyEvent.getKeyCode() == KeyEvent.VK_DOWN
            || keyEvent.getKeyCode() == KeyEvent.VK_HOME
            || keyEvent.getKeyCode() == KeyEvent.VK_END
            || keyEvent.getKeyCode() == KeyEvent.VK_PAGE_UP
            || keyEvent.getKeyCode() == KeyEvent.VK_PAGE_DOWN;
    }

    default boolean isDeleteKey(KeyEvent keyEvent) {
        return keyEvent.getKeyCode() == KeyEvent.VK_DELETE
            || keyEvent.getKeyCode() == KeyEvent.VK_BACK_SPACE;
    }

    default boolean isEditorKey(KeyEvent keyEvent) {
        return keyEvent.getKeyCode() == VK_ENTER
            || keyEvent.getKeyCode() == VK_TAB
            || keyEvent.getKeyCode() == VK_DELETE
            || keyEvent.getKeyCode() == VK_BACK_SPACE;
    }

    default boolean isLetter(char character) {
        return character >= ' ' && character <= '~';
    }

    default Optional<Character> getCharAtPosition(int position) {
        return ofNullable(getDocument())
            .filter(document -> position < document.getLength())
            .map(document -> {
                try {
                    return document
                        .getText(position, 1)
                        .charAt(0);
                } catch (BadLocationException e) {
                    // ignore
                    return null;
                }
            });
    }

    default Optional<Rectangle2D> getRectangleAtPosition(int position) {
        try {
            return ofNullable(modelToView2D(position));
        } catch (BadLocationException e) {
            // ignore
        }
        return empty();
    }

    default double getCharWidthAtPosition(int position) {
        return getFontMetrics(getFont())
            .charWidth(getCharAtPosition(position)
                .orElse(' '));
    }

    default FontMetrics getFontMetrics() {
        return getFontMetrics(getFont());
    }

    default Document getDocument() {
        return null;
    }

    default Rectangle2D modelToView2D(int position) throws BadLocationException {
        return null;
    }

    FontMetrics getFontMetrics(Font font);

    Font getFont();
}
