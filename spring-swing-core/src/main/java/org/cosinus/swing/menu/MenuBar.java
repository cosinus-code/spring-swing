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

import org.cosinus.swing.form.FormComponent;
import org.cosinus.swing.translate.Translator;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.stream;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Menu bar model
 */
public class MenuBar extends JMenuBar implements FormComponent {

    private static final String SEPARATOR = "separator";

    @Autowired
    protected Translator translator;

    private BoxMenu boxMenu;

    private final MenuModel menuModel;

    private final boolean withBoxMenu;

    private final ActionListener actionListener;

    private Map<String, AbstractButton> menuComponentsMap;

    public MenuBar(MenuModel menuModel,
                   boolean withBoxMenu,
                   ActionListener actionListener) {
        injectContext(this);

        this.withBoxMenu = withBoxMenu;
        this.menuModel = menuModel;
        this.actionListener = actionListener;
        this.menuComponentsMap = new HashMap<>();
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

        menuModel.forEach((menuKey, menuMap) -> {
            Menu menu = new Menu(menuKey, withBoxMenu);
            add(menu);
            menuComponentsMap.putIfAbsent(menuKey, menu);
            menuMap.forEach((menuItemKey, menuItemModel) -> {
                if (menuItemKey.startsWith(SEPARATOR)) {
                    menu.add(new JSeparator());
                } else {
                    MenuItem menuItem = new MenuItem(actionListener,
                        menuItemKey,
                        KeyStroke.getKeyStroke(menuItemModel.getShortcut()),
                        withBoxMenu);
                    menu.add(menuItem);
                    menuComponentsMap.putIfAbsent(menuItemKey, menuItem);
                }
            });
        });
    }

    public MenuModel getMenuModel() {
        return menuModel;
    }

    public AbstractButton getMenuComponent(String menuKey) {
        return menuComponentsMap.get(menuKey);
    }

    @Override
    public void translate() {
        stream(getSubElements())
            .filter(FormComponent.class::isInstance)
            .map(FormComponent.class::cast)
            .forEach(FormComponent::translate);
    }
}
