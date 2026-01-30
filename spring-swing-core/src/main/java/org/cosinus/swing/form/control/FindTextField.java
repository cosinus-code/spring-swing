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

package org.cosinus.swing.form.control;

import org.cosinus.swing.find.FindText;
import org.cosinus.swing.ui.ApplicationUIHandler;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import static java.awt.Color.*;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.util.Optional.ofNullable;
import static javax.swing.SwingUtilities.invokeLater;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;
import static org.cosinus.swing.find.FindText.buildFindText;

public class FindTextField extends JTextField
    implements Control<FindText>, KeyListener, MouseListener, MouseMotionListener, ActionListener {

    public static final int BUTTON_SIZE = 20;

    public static final String CASE_SENSITIVE_TEXT = "Cc";
    public static final String WHOLE_WORD_TEXT = "W";
    public static final String REGULAR_EXPRESSION_TEXT = ".*";

    public static final Color SELECTED_FOREGROUND_COLOR = darkGray;
    public static final Color UNSELECTED_FOREGROUND_COLOR = lightGray;

    @Autowired
    private ApplicationUIHandler uiHandler;

    private FindText value;

    private final Runnable action;

    private boolean actionOnSettingChange;

    public FindTextField() {
        this(null, null);
    }

    public FindTextField(final Runnable action) {
        this(null, action);
    }

    public FindTextField(String text, final Runnable action) {
        super(text);
        injectContext(this);
        this.action = action;
        this.value = new FindText(text);

        setBorder(new CompoundBorder(
            getBorder(),
            emptyBorder(0, 0, 0, 3 * BUTTON_SIZE)));
        addMouseListener(this);
        addMouseMotionListener(this);
        addActionListener(this);
    }

    public void setActionOnSettingChange(boolean actionOnSettingChange) {
        this.actionOnSettingChange = actionOnSettingChange;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int fontHeight = g.getFontMetrics().getHeight();
        int height = (getHeight() + fontHeight) / 2;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);

        g.setColor(getForegroundColor(value.isCaseSensitive()));
        g.drawString(CASE_SENSITIVE_TEXT, getWidth() - 3 * BUTTON_SIZE, height - 3);

        g.setColor(getForegroundColor(value.isWholeWord()));
        g.drawString(WHOLE_WORD_TEXT, getWidth() - 2 * BUTTON_SIZE, height - 3);

        g.setColor(getForegroundColor(value.isRegularExpression()));
        g.drawString(REGULAR_EXPRESSION_TEXT, getWidth() - BUTTON_SIZE, height - 3);
    }

    private Color getForegroundColor(boolean selected) {
        return selected ?
            !SELECTED_FOREGROUND_COLOR.equals(getBackground()) ? SELECTED_FOREGROUND_COLOR : white :
            !UNSELECTED_FOREGROUND_COLOR.equals(getBackground()) ? UNSELECTED_FOREGROUND_COLOR : black;
    }

    @Override
    public FindText getControlValue() {
        return buildFindText(getText())
            .caseSensitive(value.isCaseSensitive())
            .wholeWord(value.isWholeWord())
            .regularExpression(value.isRegularExpression());
    }

    @Override
    public void setControlValue(FindText value) {
        this.value = value;
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {
        if (keyEvent.getKeyChar() == VK_ENTER) {
            performAction();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        if (isMouseOverCaseSensitiveToggle(mouseEvent)) {
            value.caseSensitive(!value.isCaseSensitive());
        } else if (isMouseOverWholeWordToggle(mouseEvent)) {
            value.wholeWord(!value.isWholeWord());
        } else if (isMouseOverRegularExpressionToggle(mouseEvent)) {
            value.regularExpression(!value.isRegularExpression());
        } else {
            return;
        }
        invokeLater(this::repaint);
        if (actionOnSettingChange) {
            performAction();
        }
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {
    }

    @Override
    public void mouseMoved(final MouseEvent mouseEvent) {
        setCursor(isMouseOverToggleButtons(mouseEvent) ? uiHandler.getHandCursor() : uiHandler.getDefaultCursor());
    }

    private boolean isMouseOverToggleButtons(final MouseEvent mouseEvent) {
        return mouseEvent.getX() > getWidth() - getBorderRightInset();
    }

    private boolean isMouseOverCaseSensitiveToggle(final MouseEvent mouseEvent) {
        int width = getWidth() - getBorderRightInset();
        return mouseEvent.getX() > width && mouseEvent.getX() < width + BUTTON_SIZE;
    }

    private boolean isMouseOverWholeWordToggle(final MouseEvent mouseEvent) {
        int width = getWidth() - getBorderRightInset() + BUTTON_SIZE;
        return mouseEvent.getX() > width && mouseEvent.getX() < width + BUTTON_SIZE;
    }

    private boolean isMouseOverRegularExpressionToggle(final MouseEvent mouseEvent) {
        int width = getWidth() - getBorderRightInset() + 2 * BUTTON_SIZE;
        return mouseEvent.getX() > width && mouseEvent.getX() < width + BUTTON_SIZE;
    }

    private int getBorderRightInset() {
        return getBorder().getBorderInsets(this).right;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        performAction();
    }

    public void performAction() {
        ofNullable(action)
            .ifPresent(Runnable::run);
    }

    @Override
    public void addActionListener(ActionListener actionListener) {
        super.addActionListener(actionListener);
    }
}
