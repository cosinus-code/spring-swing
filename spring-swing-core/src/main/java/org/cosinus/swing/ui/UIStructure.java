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
package org.cosinus.swing.ui;

import org.cosinus.swing.form.Panel;
import org.cosinus.swing.form.control.Button;
import org.cosinus.swing.form.control.Control;
import org.cosinus.swing.form.control.Label;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.awt.BorderLayout.*;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.ui.UILayout.VERTICAL_FLOW;

public class UIStructure extends Panel {

    public static final String OK_BUTTON_ID = "ok";

    public static final String CANCEL_BUTTON_ID = "cancel";

    private final UIDescriptor uiDescriptor;

    private final Map<String, Control<?>> fieldssMap;

    private final Map<String, Button> buttonsMap;

    private Button defaultButton;

    private Panel fieldsPanel;

    private Panel buttonsPanel;

    public UIStructure(UIDescriptor uiDescriptor) {
        super(new BorderLayout());

        this.uiDescriptor = uiDescriptor;
        this.fieldssMap = new HashMap<>();
        this.buttonsMap = new HashMap<>();
    }

    @Override
    public void initComponents() {
        setBorder(emptyBorder(5, 10, 5, 10));

        ofNullable(uiDescriptor.getTitle())
            .map(Label::new)
            .ifPresent(titleLabel -> {
                Panel titlePanel = new Panel();
                titlePanel.setBorder(emptyBorder(10, 10, 10, 10));
                titlePanel.add(titleLabel);
                add(titlePanel, NORTH);
            });

        int fieldsCount = uiDescriptor.getFields().size();
        if (fieldsCount > 0) {
            fieldsPanel = new Panel();
            fieldsPanel.setLayout(ofNullable(uiDescriptor.getLayout())
                .orElse(VERTICAL_FLOW)
                .layoutManager(fieldsPanel, fieldsCount));
            fieldsPanel.setBorder(emptyBorder(0, 0, 10, 0));

            Panel centerPanel = new Panel(new BorderLayout());
            centerPanel.add(fieldsPanel, NORTH);
            add(centerPanel, CENTER);
        }

        int buttonsCount = uiDescriptor.getButtons().size();
        if (buttonsCount > 0) {
            buttonsPanel = new Panel(new GridLayout(1, buttonsCount, 4, 0));
            buttonsPanel.setBorder(emptyBorder(10, 0, 0, 0));
            Panel southPanel = new Panel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            southPanel.add(buttonsPanel);
            add(southPanel, SOUTH);
        }
    }

    public void addField(String id, @NotNull Control<?> field, Label label) {
        if (label != null) {
            label.setVerticalAlignment(SwingConstants.BOTTOM);
            fieldsPanel.add(label);
        }
        fieldsPanel.add(field.getComponent());
        fieldssMap.put(id, field);
    }

    public void addButton(String id, @NotNull Button button) {
        buttonsPanel.add(button);
        buttonsMap.put(id, button);
    }

    public Optional<Control<?>> getField(String id) {
        return ofNullable(fieldssMap.get(id));
    }

    public Optional<Button> getButton(String id) {
        return ofNullable(buttonsMap.get(id));
    }

    public Optional<Button> getDefaultButton() {
        return ofNullable(defaultButton)
            .or(() -> getButton(OK_BUTTON_ID));
    }

    public void setDefaultButton(Button defaultButton) {
        this.defaultButton = defaultButton;
    }

    public void setDefaultButton(String defaultButtonId) {
        getButton(defaultButtonId)
            .ifPresent(this::setDefaultButton);
    }

    public Optional<Object> getValue(String id) {
        return getField(id)
            .map(Control::getControlValue);
    }
}
