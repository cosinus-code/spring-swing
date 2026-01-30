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
package org.cosinus.swing.ui;

import lombok.Getter;
import lombok.Setter;
import org.cosinus.swing.error.SpringSwingException;
import org.cosinus.swing.form.Panel;
import org.cosinus.swing.form.control.*;
import org.cosinus.swing.form.control.Button;
import org.cosinus.swing.form.control.Label;
import org.cosinus.swing.layout.SpringGridLayout;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import static java.awt.BorderLayout.*;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.border.Borders.emptyBorder;
import static org.cosinus.swing.ui.UILayout.SPRING_GRID;
import static org.cosinus.swing.ui.UILayout.VERTICAL_FLOW;

public class UIStructure extends Panel {

    public static final String OK_BUTTON_ID = "ok";

    public static final String CANCEL_BUTTON_ID = "cancel";

    private final UIDescriptor uiDescriptor;

    private final Map<String, Control<?>> controlsMap;

    private final Map<String, Button> buttonsMap;

    @Getter
    private final Map<String, Set<Control<?>>> actionControls;

    @Getter
    private final Set<Control<?>> doubleClickActionControls = new HashSet<>();

    private Button defaultButton;

    private Panel controlsPanel;

    private Panel buttonsPanel;

    private Panel iconPanel;

    @Setter
    private Component focusComponent;

    public UIStructure(UIDescriptor uiDescriptor) {
        super(new BorderLayout());

        this.uiDescriptor = uiDescriptor;
        this.controlsMap = new HashMap<>();
        this.buttonsMap = new HashMap<>();
        this.actionControls = new HashMap<>();
    }

    @Override
    public void initComponents() {
        setLayout(new BorderLayout(5, 5));
        setBorder(emptyBorder(5, 10, 5, 10));

        ofNullable(uiDescriptor.getTitle())
            .map(Label::new)
            .ifPresent(titleLabel -> {
                Panel titlePanel = new Panel();
                titlePanel.setBorder(emptyBorder(10, 10, 10, 10));
                titlePanel.add(titleLabel);
                add(titlePanel, NORTH);
            });

        ofNullable(uiDescriptor.getIcon()).ifPresent(uiIcon -> {
            iconPanel = new Panel(new BorderLayout());
            add(iconPanel, WEST);
        });

        int fieldsCount = uiDescriptor.getFields().size();
        if (fieldsCount > 0) {
            controlsPanel = new Panel();
            controlsPanel.setLayout(ofNullable(uiDescriptor.getLayout())
                .orElse(SPRING_GRID)
                .layoutManager(controlsPanel, fieldsCount));
            controlsPanel.setBorder(emptyBorder(0, 0, 10, 0));

            Panel centerPanel = new Panel(new BorderLayout());
            centerPanel.add(controlsPanel, NORTH);
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

    public void pack() {
        if (controlsPanel.getLayout() instanceof SpringGridLayout layout) {
            layout.pack();
        }
    }

    public void addControl(String id, Control<?> control, Label label, UIField field) {
        if (label != null) {
            if (uiDescriptor.getLayout() == VERTICAL_FLOW) {
                label.setVerticalAlignment(SwingConstants.BOTTOM);
            }
            controlsPanel.add(label);
        }
        if (field.getWidth() > 0 || field.getHeight() > 0 ) {
            ScrollPane scroll = new ScrollPane();
            scroll.setPreferredSize(new Dimension(field.getWidth(), field.getHeight()));
            scroll.add(control.getComponent());
            controlsPanel.add(scroll);
        } else {
            controlsPanel.add(control.getComponent());
        }
        controlsMap.put(id, control);
    }

    public void addButton(String id, Button button) {
        buttonsPanel.add(button);
        buttonsMap.put(id, button);
    }

    public Optional<Control<?>> findControl(String id) {
        return ofNullable(controlsMap.get(id));
    }

    public Optional<Button> findButton(String id) {
        return ofNullable(buttonsMap.get(id));
    }

    public Optional<Button> findDefaultButton() {
        return ofNullable(defaultButton)
            .or(() -> findButton(OK_BUTTON_ID));
    }

    public void setDefaultButton(Button defaultButton) {
        this.defaultButton = defaultButton;
    }

    public void setDefaultButton(String defaultButtonId) {
        findButton(defaultButtonId)
            .ifPresent(this::setDefaultButton);
    }

    public Object getValue(String id) {
        return getControl(id).getControlValue();
    }

    public Control<?> getControl(String id) {
        return ofNullable(controlsMap.get(id))
            .orElseThrow(() -> new SpringSwingException("Cannot find control with id: " + id));
    }

    public void addActionComponent(String action, Control<?> control) {
        actionControls
            .computeIfAbsent(action, key -> new HashSet<>())
            .add(control);
    }

    public Optional<Component> getFocusComponent() {
        return ofNullable(focusComponent);
    }

    public void addIcon(final UIIcon uiIcon, final Control<?> iconControl) {
        iconPanel.add(iconControl.getComponent(), NORTH);
        controlsMap.put(uiIcon.getId(), iconControl);
    }

    public Optional<String> getIconId() {
        return ofNullable(uiDescriptor.getIcon())
            .map(UIIcon::getId);
    }

    public Set<Control<?>> getControlsWithAction(final String action) {
        return actionControls.getOrDefault(action, emptySet());
    }

    public void addDoubleClickActionComponents(Control<?> control) {
        doubleClickActionControls.add(control);
    }
}
