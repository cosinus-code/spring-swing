/*
 * Copyright 2020 Cosinus Software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.swing.form.control;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.*;

import static java.awt.Cursor.HAND_CURSOR;
import static java.awt.Cursor.TEXT_CURSOR;
import static java.awt.event.KeyEvent.VK_ENTER;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Extension of {@link JTextField} with icon.
 */
public abstract class IconTextField extends JTextField implements MouseListener, MouseMotionListener, KeyListener {

    private static final int ICON_X_GAP = 2;

    private Icon icon;

    public IconTextField() {
        injectContext(this);
        initIcon();
        if (icon != null) {
            setBorder(new CompoundBorder(
                getBorder(),
                emptyBorder(0, 0, 0, icon.getIconWidth() + 2 * ICON_X_GAP)));
        }
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    protected void initIcon() {
        this.icon = getIcon();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (icon != null) {
            paintIcon(g, icon);
        }
    }

    private void paintIcon(Graphics g, Icon icon) {
        Insets iconInsets = getBorder().getBorderInsets(this);
        icon.paintIcon(this, g,
            getWidth() - iconInsets.right + ICON_X_GAP,
            (this.getHeight() - icon.getIconHeight()) / 2);
    }

    public boolean isMouseOverIcon(MouseEvent e) {
        Insets iconInsets = getBorder().getBorderInsets(this);
        return e.getX() > getWidth() - iconInsets.right + ICON_X_GAP;
    }

    @Override
    public void updateUI() {
        super.updateUI();
        initIcon();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (isMouseOverIcon(e)) {
            performAction();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        setCursor(Cursor.getPredefinedCursor(isMouseOverIcon(e) ? HAND_CURSOR : TEXT_CURSOR));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }


    @Override
    public void keyTyped(KeyEvent e) {
        if (actionOnEnter() && e.getKeyChar() == VK_ENTER) {
            performAction();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    protected boolean actionOnEnter() {
        return false;
    }

    protected abstract Icon getIcon();

    protected abstract void performAction();
}
