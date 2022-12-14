package org.cosinus.swing.form.control;

import java.util.List;

public interface AutocompleteProvider<T extends AutocompleteItem> {

    List<T> getAutocompleteItems(String text);
}
