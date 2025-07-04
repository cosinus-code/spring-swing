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

import org.cosinus.swing.action.ActionProducer;
import org.cosinus.swing.form.FormComponent;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.event.ActionListener;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;


/**
 * Menu item model
 */
public class MenuItem extends JMenuItem implements FormComponent, ActionProducer, DuplicateMenuHolder {

    @Autowired
    protected Translator translator;

    private JMenuItem altMenuItem;

    private final String key;

    public MenuItem(ActionListener actionListener, String key) {
        this(actionListener, key, null, false);
    }

    public MenuItem(ActionListener actionListener,
                    String key,
                    KeyStroke keyStroke,
                    boolean duplicate) {
        super();
        injectContext(this);

        this.key = key;
        super.addActionListener(actionListener);
        super.setAccelerator(keyStroke);

        if (duplicate) {
            altMenuItem = new MenuItem(actionListener,
                                       key,
                                       keyStroke,
                                       false);
            altMenuItem.addActionListener(actionListener);
            altMenuItem.setAccelerator(keyStroke);
        }
    }

    public void setText(String text) {
        if (altMenuItem != null) {
            altMenuItem.setText(text);
        }
        super.setText(text);
    }

    @Override
    public JMenuItem getDuplicateMenu() {
        return altMenuItem;
    }

    @Override
    public String getActionKey() {
        return key;
    }

    @Override
    public void initComponents() {
    }

    @Override
    public void translate() {
        setText(translator.translate(key));
    }
}
