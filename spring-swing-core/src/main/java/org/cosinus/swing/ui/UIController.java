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

import org.cosinus.swing.form.control.*;
import org.cosinus.swing.form.control.Button;
import org.cosinus.swing.form.control.Label;
import org.cosinus.swing.form.control.provider.ControlProvider;
import org.cosinus.swing.translate.Translator;

import java.awt.*;

import static java.util.Optional.ofNullable;

public class UIController {

    public static final String SWING_UI_INITIALIZER_PROPERTY = "swing.ui.initializer";

    private final UIDescriptorProvider uiDescriptorProvider;

    private final Translator translator;

    public UIController(final UIDescriptorProvider uiDescriptorProvider,
                        final Translator translator) {
        this.uiDescriptorProvider = uiDescriptorProvider;
        this.translator = translator;
    }

    public UIStructure createUiStructure(String uiDescriptorName) {
        UIDescriptor uiDescriptor = uiDescriptorProvider.getUIDescriptor(uiDescriptorName);

        UIStructure uiStructure = new UIStructure(uiDescriptor);
        uiStructure.initComponents();

        ofNullable(uiDescriptor.getIcon()).ifPresent(uiIcon -> {
            Control<?> control = uiIcon.isEditable() ? new IconButton() : new Label();
            if (uiIcon.getWidth() > 0 || uiIcon.getHeight() > 0) {
                control.getComponent().setPreferredSize(new Dimension(uiIcon.getWidth(), uiIcon.getHeight()));
            }
            if (uiIcon.isDisabled()) {
                control.setControlEnabled(false);
            }
            uiStructure.addIcon(uiIcon, control);
        });

        uiDescriptor.getFields()
            .forEach(field -> ofNullable(createControl(field))
                .ifPresent(control -> {
                    Label label = ofNullable(field.getLabel())
                        .map(translator::translate)
                        .map(control::createAssociatedLabel)
                        .orElse(null);
                    if (field.isDisabled()) {
                        control.setControlEnabled(false);
                    }
                    if (control instanceof Component component) {
                        if (field.isAction()) {
                            uiStructure.addActionComponent(component);
                        }
                        if (field.isFocus()) {
                            uiStructure.setFocusComponent(component);
                        }
                    }
                    uiStructure.addControl(field.getId(), control, label, field);
                }));
        uiDescriptor.getButtons()
            .forEach(button -> uiStructure.addButton(button.getId(), createButton(button)));
        ofNullable(uiDescriptor.getDefaultButton())
            .ifPresent(uiStructure::setDefaultButton);

        uiStructure.pack();
        return uiStructure;
    }

    public void fillUIStructure(final UIStructure uiStructure, final UIModel model) {
        uiStructure.getIconId()
            .map(uiStructure::getControl)
            .ifPresent(control -> {
                if (control instanceof Label label) {
                    label.setIcon(model.getIcon());
                } else if (control instanceof IconButton button) {
                    button.setIcon(model.getIcon());
                }
            });
        model.keys()
            .forEach(key -> ofNullable(uiStructure.getControl(key))
                .ifPresent(control -> fillControl(key, control, model)));
    }

    public <T> void fillControl(String key, final Control<T> control, final UIModel model) {
        if (control instanceof MultipleValuesControl<?> multipleValuesControl) {
            ofNullable(model.getValues(key))
                .ifPresent(values -> ((MultipleValuesControl<T>) multipleValuesControl).setValues((T[]) values));
        }
        ofNullable(model.getValue(key))
            .ifPresent(value -> control.setControlValue((T) value));
    }

    public void updateUIModel(final UIStructure uiStructure, final UIModel model) {
        model.keys()
            .forEach(key -> ofNullable(uiStructure.getControl(key))
                .ifPresent(control -> model.putValue(key, control.getControlValue())));
    }

    protected Control<?> createControl(UIField field) {
        return ofNullable(field)
            .map(UIField::getType)
            .map(ControlType::getControlProvider)
            .map(ControlProvider::getControl)
            .orElse(null);
    }

    protected Button createButton(final UIButton uiButton) {
        final Button button = new Button();
        ofNullable(uiButton.getLabel())
            .map(translator::translate)
            .ifPresent(button::setText);
        return button;
    }
}
