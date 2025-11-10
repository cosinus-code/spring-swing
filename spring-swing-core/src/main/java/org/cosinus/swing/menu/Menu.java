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

import org.cosinus.swing.form.FormComponent;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Menu list model
 */
public class Menu extends JMenu implements FormComponent, DuplicateMenuHolder {

    @Autowired
    protected Translator translator;

    private JMenu duplicateMenu;

    private final String key;

    private final List<FormComponent> formComponents;

    public Menu(String key) {
        this(key, false);
    }

    public Menu(String key, boolean duplicate) {
        super();
        injectContext(this);

        this.key = key;
        this.formComponents = new ArrayList<>();

        if (duplicate) {
            duplicateMenu = new JMenu();
        }
    }

    public void add(JSeparator separator) {
        if (duplicateMenu != null) {
            duplicateMenu.add(new JSeparator());
        }
        super.add(separator);
    }

    @Override
    public JMenuItem add(JMenuItem menuItem) {
        if (menuItem instanceof FormComponent formComponent) {
            formComponents.add(formComponent);
        }
        if (menuItem instanceof DuplicateMenuHolder duplicateMenuHolder) {
            if (duplicateMenu != null) {
                duplicateMenu.add(duplicateMenuHolder.getDuplicateMenu());
            }
        }
        return super.add(menuItem);
    }

    @Override
    public void setText(String text) {
        if (duplicateMenu != null) {
            duplicateMenu.setText(text);
        }
        super.setText(text);
    }

    @Override
    public JMenu getDuplicateMenu() {
        return duplicateMenu;
    }

    @Override
    public void initComponents() {

    }

    @Override
    public void translate() {
        setText(translator.translate(key));
        formComponents.forEach(FormComponent::translate);
    }

    public boolean isEmpty() {
        return formComponents.isEmpty();
    }
}
