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

/**
 * Menu list model
 */
@SuppressWarnings("serial")
public class Menu extends JMenu implements Translatable {

    private JMenu altMenu;

    private String key;

    /**
     * Creates a new instance of Menu
     */
    public Menu(String key,
                boolean alt) {
        super();
        this.key = key;
        if (alt) {
            altMenu = new JMenu();
        }
    }

    public void add(JSeparator separator) {
        if (altMenu != null) {
            altMenu.add(new JSeparator());
        }
        super.add(separator);
    }

    public JMenuItem add(MenuItem menuItem) {
        if (altMenu != null) {
            altMenu.add(menuItem.getAltMenuItem());
        }
        return super.add(menuItem);
    }

    public JMenuItem add(Menu menu) {
        if (altMenu != null) {
            altMenu.add(menu.getAltMenu());
        }
        return super.add(menu);
    }

    @Override
    public void setText(String text) {
        if (altMenu != null) {
            altMenu.setText(text);
        }
        super.setText(text);
    }

    public JMenu getAltMenu() {
        return altMenu;
    }

    @Override
    public void translate(Translator translator) {
        setText(translator.translate(key));
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i) instanceof Translatable) {
                ((Translatable) getItem(i)).translate(translator);
            }
        }
    }
}
