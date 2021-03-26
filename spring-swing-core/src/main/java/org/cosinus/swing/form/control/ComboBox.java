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

package org.cosinus.swing.form.control;

import javax.swing.*;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

public class ComboBox<T> extends JComboBox<T> implements Control<T> {

    public ComboBox(ComboBoxModel<T> aModel) {
        super(aModel);
        injectContext(this);
    }

    public ComboBox(T[] items) {
        super(items);
        injectContext(this);
    }

    public ComboBox(T[] items, T selectedItem) {
        super(items);
        injectContext(this);
        setSelectedItem(selectedItem);
    }

    public ComboBox() {
        injectContext(this);
    }

    @Override
    public T getValue() {
        return (T) getSelectedItem();
    }

    @Override
    public void setValue(T value) {
        setSelectedItem(value);
    }
}
