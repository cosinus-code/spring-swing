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

package org.cosinus.swing.menu;

import org.cosinus.swing.action.ActionProducer;
import org.cosinus.swing.form.FormComponent;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.event.ActionListener;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Checkbox menu model
 */
public class CheckBoxMenuItem extends JCheckBoxMenuItem implements FormComponent, ActionProducer {

    @Autowired
    protected Translator translator;

    private final JCheckBoxMenuItem altMenuItem;

    private final String key;

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
        super();
        injectContext(this);

        this.key = key;

        super.addActionListener(action);
        super.setAccelerator(keyStroke);
        super.setSelected(selected);

        altMenuItem = new JCheckBoxMenuItem();
        altMenuItem.addActionListener(action);
        altMenuItem.setAccelerator(keyStroke);
    }

    public void setText(String text) {
        altMenuItem.setText(text);
        super.setText(text);
    }

    public JCheckBoxMenuItem getAltMenuItem() {
        return altMenuItem;
    }

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
