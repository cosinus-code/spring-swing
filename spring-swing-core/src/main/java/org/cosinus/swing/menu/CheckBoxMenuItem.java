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

package org.cosinus.swing.menu;

import org.cosinus.swing.action.ActionController;
import org.cosinus.swing.action.ActionProducer;
import org.cosinus.swing.action.SwingAction;
import org.cosinus.swing.form.FormComponent;
import org.cosinus.swing.icon.IconHolder;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.event.ActionListener;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Checkbox menu model
 * <p>
 * For a toggle action (see {@link SwingAction#getSelectedState()}),
 * {@link #refreshSelected()} shows the current state of the action.
 */
public class CheckBoxMenuItem extends JCheckBoxMenuItem
    implements FormComponent, ActionProducer, DuplicateMenuHolder, IconHolder {

    @Autowired
    protected Translator translator;

    @Autowired
    protected ActionController actionController;

    private final CheckBoxMenuItem altMenuItem;

    private final String key;

    private final String iconName;

    public CheckBoxMenuItem(ActionListener action,
                            String key) {
        this(action,
            key,
            false,
            null);
    }

    public CheckBoxMenuItem(ActionListener action,
                            boolean selected,
                            String key) {
        this(action,
            key,
            selected,
            null);
    }

    public CheckBoxMenuItem(ActionListener action,
                            String key,
                            boolean selected,
                            KeyStroke keyStroke) {
        this(action, key, selected, keyStroke, false);
    }

    public CheckBoxMenuItem(ActionListener action,
                            String key,
                            boolean selected,
                            KeyStroke keyStroke,
                            boolean duplicate) {
        super();
        injectContext(this);

        this.key = key;

        super.addActionListener(action);
        super.setAccelerator(keyStroke);
        super.setSelected(selected);

        altMenuItem = duplicate ?
            new CheckBoxMenuItem(action, key, selected, keyStroke, false) :
            null;

        this.iconName = actionController.findAction(key)
            .map(SwingAction::getIconName)
            .orElse(null);
    }

    public void setText(String text) {
        if (altMenuItem != null) {
            altMenuItem.setText(text);
        }
        super.setText(text);
    }

    /**
     * Show the current state of the action, if it is a toggle action.
     */
    public void refreshSelected() {
        actionController.findAction(key)
            .flatMap(SwingAction::getSelectedState)
            .ifPresent(selected -> {
                setSelected(selected);
                if (altMenuItem != null) {
                    altMenuItem.setSelected(selected);
                }
            });
    }

    @Override
    public JMenuItem getDuplicateMenu() {
        return altMenuItem;
    }

    public String getActionKey() {
        return key;
    }

    @Override
    public void translate() {
        setText(translator.translate(key));
    }

    @Override
    public String getIconName() {
        return iconName;
    }
}
