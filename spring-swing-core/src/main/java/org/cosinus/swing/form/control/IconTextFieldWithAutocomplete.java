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
            return;
        }
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
