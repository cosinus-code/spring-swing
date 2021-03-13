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

package org.cosinus.swing.form.menu;

import org.cosinus.swing.context.SwingApplicationContext;
import org.cosinus.swing.context.SwingInject;
import org.cosinus.swing.form.FormComponent;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.event.ActionListener;

import static java.util.Arrays.stream;

/**
 * Menu bar model
 */
public class MenuBar extends JMenuBar implements SwingInject, FormComponent {

    private static final String SEPARATOR = "separator";

    @Autowired
    protected Translator translator;

    private BoxMenu boxMenu;

    private final MenuModel mapModel;

    private final boolean withBoxMenu;

    private final ActionListener actionListener;

    public MenuBar(MenuModel mapModel,
                   boolean withBoxMenu,
                   ActionListener actionListener) {
        injectSwingContext(SwingApplicationContext.instance);

        this.withBoxMenu = withBoxMenu;
        this.mapModel = mapModel;
        this.actionListener = actionListener;
    }

    public void add(Menu menu) {
        if (boxMenu != null) {
            boxMenu.add(menu);
        }
        super.add(menu);
    }

    @Override
    public void initComponents() {
        if (withBoxMenu) {
            boxMenu = new BoxMenu();
        }

        mapModel.forEach((menuKey, menuMap) -> {
            Menu menu = new Menu(menuKey, withBoxMenu);
            add(menu);
            menuMap.forEach((menuItemKey, menuItemShortcut) -> {
                if (menuItemKey.startsWith(SEPARATOR)) {
                    menu.add(new JSeparator());
                } else {
                    menu.add(new MenuItem(actionListener,
                                          menuItemKey,
                                          KeyStroke.getKeyStroke(menuItemShortcut),
                                          withBoxMenu));
                }
            });
        });
    }

    @Override
    public void initContent() {

    }

    @Override
    public void translate() {
        stream(getSubElements())
            .filter(FormComponent.class::isInstance)
            .map(FormComponent.class::cast)
            .forEach(FormComponent::translate);
    }
}
