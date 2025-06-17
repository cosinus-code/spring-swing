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

package org.cosinus.swing.dialog;

import org.apache.commons.lang3.ArrayUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import static java.awt.event.KeyEvent.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;
import static javax.swing.BorderFactory.createEmptyBorder;

/**
 * Custom Dialog with options to choose
 */
public class OptionsDialog extends JOptionPane {

    private int rows = 1;

    public OptionsDialog(Object message,
                         int messageType,
                         int optionType) {
        this(message,
             messageType,
             optionType,
             null,
             null,
             null);
    }

    public OptionsDialog(Object message,
                         int messageType,
                         int optionType,
                         Icon icon,
                         Object[] options) {
        this(message,
             messageType,
             optionType,
             icon,
             options,
             !ArrayUtils.isEmpty(options) ? options[0] : null);
    }

    public OptionsDialog(Object message,
                         int messageType,
                         int optionType,
                         Icon icon,
                         Object[] options,
                         Object initialValue) {
        super(message,
              messageType,
              optionType,
              icon,
              options,
              initialValue);
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public Object showDialog(Component parentComponent,
                             String title) throws HeadlessException {
        return showDialog(parentComponent,
                          title,
                          0);
    }

    public Object showDialog(Component parentComponent,
                             String title,
                             int width) throws HeadlessException {
        final JDialog dialog = createDialog(parentComponent,
                                            title);
        final JPanel panButtons = (JPanel) getComponent(getComponentCount() - 1);

        final int cols = panButtons.getComponentCount() / rows;

        range(0, panButtons.getComponentCount())
            .filter(index -> panButtons.getComponent(index) instanceof JButton)
            .forEach(index -> {
                final JButton button = (JButton) panButtons.getComponent(index);
                button.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == VK_LEFT) {
                            button.transferFocusBackward();
                        } else if (e.getKeyCode() == VK_RIGHT) {
                            button.transferFocus();
                        } else if (e.getKeyCode() == VK_ESCAPE) {
                            dialog.dispose();
                        } else if (e.getKeyCode() == VK_ENTER) {
                            button.doClick();
                        } else if (e.getKeyCode() == VK_UP) {
                            getButtonUpFromIndex(index, panButtons).requestFocusInWindow();
                        } else if (e.getKeyCode() == VK_DOWN) {
                            getButtonDownFromIndex(index, panButtons).requestFocusInWindow();
                        }
                    }
                });
            });

        panButtons.setLayout(
            rows > 1 ?
                new GridLayout(rows, cols, 5, 5) :
                new FlowLayout());
        panButtons.setBorder(createEmptyBorder(0, 5, 0, 5));

        dialog.setResizable(true);
        dialog.pack();
        if (width > 0) {
            dialog.setSize(width, dialog.getHeight());
        }
        dialog.setLocationRelativeTo(parentComponent);
        dialog.setVisible(true);
        return getValue();
    }

    private Component getButtonUpFromIndex(int index, JPanel panButtons) {
        int new_index;
        if (index == 0) {
            new_index = panButtons.getComponentCount() - 1;
        } else {
            new_index = index - rows;
            if (new_index < 0) {
                new_index = panButtons.getComponentCount() + new_index - 1;
            }
        }
        return panButtons.getComponent(new_index);
    }

    private Component getButtonDownFromIndex(int index, JPanel panButtons) {
        int new_index;
        if (index == panButtons.getComponentCount() - 1) {
            new_index = 0;
        } else {
            new_index = index + rows;
            if (new_index >= panButtons.getComponentCount()) {
                new_index = new_index - panButtons.getComponentCount() + 1;
            }
        }

        return panButtons.getComponent(new_index);
    }

    public void setComponentOrientation(Component parentComponent) {
        setComponentOrientation(
            ofNullable(parentComponent)
                .orElseGet(JOptionPane::getRootFrame)
                .getComponentOrientation());
    }
}
