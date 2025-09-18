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

package org.cosinus.swing.form.control;

import org.cosinus.swing.menu.MenuItem;
import org.cosinus.swing.menu.PopupMenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.List;

import static java.awt.event.KeyEvent.VK_ENTER;
import static java.util.Optional.ofNullable;

public abstract class IconTextFieldWithAutocomplete<T extends AutocompleteItem> extends IconTextField implements ActionListener {

    private final PopupMenu autocompleteMenu;

    public IconTextFieldWithAutocomplete() {
        this.autocompleteMenu = new PopupMenu();
    }

    @Override
    public void keyTyped(KeyEvent e) {
        if (actionOnEnter() && e.getKeyChar() == VK_ENTER) {
            performAction();
        }
    }

    protected void startAutocomplete() {
            AutocompleteProvider<T> autocompleteProvider = getAutocompleteProvider();
            if (autocompleteProvider != null) {
                new Thread(new AutocompleteLoader(autocompleteProvider)).start();
            }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ofNullable(e.getSource())
            .filter(IconTextFieldWithAutocomplete.AutocompleteMenuItem.class::isInstance)
            .map(IconTextFieldWithAutocomplete.AutocompleteMenuItem.class::cast)
            .map(AutocompleteMenuItem::getAutocompleteItem)
            .ifPresent(this::performAutocompleteItemAction);
    }

    protected abstract AutocompleteProvider<T> getAutocompleteProvider();

    protected abstract void performAutocompleteItemAction(T autocompleteItem);


    private class AutocompleteLoader implements Runnable {

        private final AutocompleteProvider<T> autocompleteProvider;

        private final String text;

        AutocompleteLoader(final AutocompleteProvider<T> autocompleteProvider) {
            this.autocompleteProvider = autocompleteProvider;
            this.text = getText();
        }

        @Override
        public void run() {
            List<T> autocompleteItems = autocompleteProvider.getAutocompleteItems(text);
            if (text.equals(getText())) {
                synchronized (autocompleteMenu) {
                    autocompleteMenu.removeAll();
                    autocompleteItems
                        .stream()
                        .map(AutocompleteMenuItem::new)
                        .forEach(autocompleteMenu::add);
                    autocompleteMenu.show(IconTextFieldWithAutocomplete.this, 0, getHeight());
                }
            }
        }
    }
    private class AutocompleteMenuItem extends MenuItem {

        private final T autocompleteItem;

        AutocompleteMenuItem(final T autocompleteItem) {
            super(IconTextFieldWithAutocomplete.this, autocompleteItem.getKey());
            this.autocompleteItem = autocompleteItem;
            setText(autocompleteItem.getDisplayName());
        }

        public T getAutocompleteItem() {
            return autocompleteItem;
        }
    }
}
