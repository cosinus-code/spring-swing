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

import org.cosinus.stream.swing.ExtendedContainer;
import org.cosinus.swing.icon.IconHolder;

import javax.swing.*;
import java.awt.*;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static java.util.stream.IntStream.range;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Extension of the {@link JList}
 * which will automatically inject the application context.
 *
 * @param <T> the type of the value
 */
public class List<T> extends JList<T> implements Control<T>, MultipleValuesControl<T>, ExtendedContainer {

    public List(ComboBoxModel<T> aModel) {
        super(aModel);
        injectContext(this);
        initCellRenderer();
    }

    public List(T[] items) {
        super(items);
        injectContext(this);
        initCellRenderer();
    }

    public List(T[] items, T selectedItem) {
        super(items);
        injectContext(this);
        setSelectedValue(selectedItem, true);
        initCellRenderer();
    }

    public List() {
        injectContext(this);
        initCellRenderer();
    }

    public void initCellRenderer() {
        setCellRenderer(new DelegateCellRenderer<>(getCellRenderer()));
    }

    @Override
    public T getControlValue() {
        return getSelectedValue();
    }

    @Override
    public void setControlValue(T value) {
        setSelectedValue(value, true);
    }

    @Override
    public void setValues(T[] values) {
        setModel(new DefaultComboBoxModel<>(values));
    }

    @Override
    public Stream<Component> streamAdditionalContainers() {
        DefaultComboBoxModel<T> model = (DefaultComboBoxModel<T>) getModel();
        return range(0, model.getSize())
            .mapToObj(model::getElementAt)
            .filter(item -> item instanceof Component)
            .map(Component.class::cast);
    }

    private static class DelegateCellRenderer<T> extends DefaultListCellRenderer {

        private final ListCellRenderer<T> delegate;

        public DelegateCellRenderer(final ListCellRenderer<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Component getListCellRendererComponent(JList list,
                                                      Object value,
                                                      int index,
                                                      boolean selected,
                                                      boolean expanded) {
            JLabel label = (JLabel) delegate.getListCellRendererComponent(
                list, (T) value, index, selected, expanded);

            if (value instanceof IconHolder iconHolder) {
                ofNullable(iconHolder.getIcon())
                    .ifPresent(label::setIcon);
                ofNullable(iconHolder.getTooltip())
                    .ifPresent(label::setToolTipText);
            }
            return label;
        }
    }
}
