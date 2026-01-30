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

import org.cosinus.swing.form.Panel;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import static java.awt.FlowLayout.LEFT;
import static java.util.stream.IntStream.range;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Implementation of a {@link Control} managing a set of check boxes
 */
public class CheckBoxList extends Panel implements Control<List<CheckBoxValue>> {

    private List<CheckBox> checkBoxes;

    public CheckBoxList() {
        injectContext(this);
        checkBoxes = new ArrayList<>();
    }

    public CheckBoxList(final List<CheckBoxValue> values) {
        injectContext(this);
        init(values);
    }

    public void init(List<CheckBoxValue> values) {
        setLayout(new FlowLayout(LEFT, 0, 0));
        checkBoxes = values
            .stream()
            .map(value -> new CheckBox(value.text(), value.selected()))
            .toList();
        checkBoxes.forEach(this::add);
    }

    @Override
    public List<CheckBoxValue> getControlValue() {
        return checkBoxes
            .stream()
            .map(checkBox -> new CheckBoxValue(checkBox.getText(), checkBox.isSelected()))
            .toList();
    }

    @Override
    public void setControlValue(List<CheckBoxValue> values) {
        if (isEmpty(checkBoxes)) {
            init(values);
        } else {
            range(0, checkBoxes.size())
                .forEach(i -> checkBoxes.get(i).setSelected(values.get(i).selected()));
        }
    }

    @Override
    public void addActionListener(ActionListener actionListener) {
        checkBoxes.forEach(checkBox -> checkBox.addActionListener(actionListener));
    }

    @Override
    public void setControlEnabled(boolean enabled) {
        checkBoxes.forEach(checkBox -> checkBox.setControlEnabled(enabled));
    }
}
