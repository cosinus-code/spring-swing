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

import org.cosinus.swing.translate.Translatable;
import org.cosinus.swing.translate.Translator;

import javax.swing.*;
import java.awt.event.ActionListener;

import static java.util.Arrays.stream;

/**
 * Menu bar model
 */
public class MenuBar extends JMenuBar implements Translatable {

    private static final String SEPARATOR = "separator";

    private BoxMenu boxMenu;

    public MenuBar(MenuModel mapModel,
                   boolean withBoxMenu,
                   ActionListener actionListener) {
        if (withBoxMenu) {
            boxMenu = new BoxMenu();
        }
        init(mapModel,
             actionListener,
             withBoxMenu);
    }

    protected void init(MenuModel mapModel,
                        ActionListener actionListener,
                        boolean withBoxMenu) {
        mapModel.forEach((menuKey, menuMap) -> {
            Menu menu = new Menu(menuKey,
                                 withBoxMenu);
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

    public void add(Menu menu) {
        if (boxMenu != null) {
            boxMenu.add(menu);
        }
        super.add(menu);
    }

    @Override
    public void translate(Translator translator) {
        stream(getSubElements())
            .filter(Translatable.class::isInstance)
            .map(Translatable.class::cast)
            .forEach(translatable -> translatable.translate(translator));
    }
}
