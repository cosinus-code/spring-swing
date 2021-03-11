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

import org.cosinus.swing.context.SwingInject;
import org.cosinus.swing.form.FormComponent;

import javax.swing.*;
import java.awt.*;

/**
 * Box menu model
 */
public class BoxMenu implements SwingInject, FormComponent {

    public static final int BOX_MENU_SIZE = 10;

    public static final Dimension BOX_MENU_DIMENSION =
            new Dimension(BOX_MENU_SIZE,
                          BOX_MENU_SIZE);

    private JMenuBar boxMenuBar;

    private JMenu boxMenu;

    public BoxMenu() {
        init();
    }

    private void init() {
        boxMenu = new JMenu();
        boxMenu.setPreferredSize(BOX_MENU_DIMENSION);
        boxMenu.setMenuLocation(BOX_MENU_SIZE,
                                BOX_MENU_SIZE - 8);

        boxMenuBar = new JMenuBar();
        boxMenuBar.add(boxMenu);
        boxMenuBar.setBorder(null);
        boxMenuBar.setPreferredSize(BOX_MENU_DIMENSION);
    }

    public void add(Menu menu) {
        boxMenu.add(menu.getDuplicateMenu());
    }

    public JMenuBar getBoxMenuBar() {
        return boxMenuBar;
    }

    @Override
    public void initComponents() {

    }

    @Override
    public void initContent() {

    }

    @Override
    public void translate() {

    }
}
