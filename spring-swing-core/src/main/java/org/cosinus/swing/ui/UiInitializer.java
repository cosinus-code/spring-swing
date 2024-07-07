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

import org.cosinus.swing.form.control.Button;
import org.cosinus.swing.form.control.Control;
import org.cosinus.swing.form.control.ControlType;
import org.cosinus.swing.form.control.Label;
import org.cosinus.swing.form.control.provider.ControlProvider;
import org.cosinus.swing.translate.Translator;

import static java.util.Optional.ofNullable;

public class UiInitializer {

    public static final String SWING_UI_INITIALIZER_PROPERTY = "swing.ui.initializer";

    private final UIDescriptorProvider uiDescriptorProvider;

    private final Translator translator;

    public UiInitializer(final UIDescriptorProvider uiDescriptorProvider,
                         final Translator translator) {
        this.uiDescriptorProvider = uiDescriptorProvider;
        this.translator = translator;
    }

    public UIStructure createUiStructure(String uiDescriptorName) {
        UIDescriptor uiDescriptor = uiDescriptorProvider.getUIDescriptor(uiDescriptorName);

        UIStructure uiStructure = new UIStructure(uiDescriptor);
        uiStructure.initComponents();

        uiDescriptor.getFields()
            .forEach(field -> ofNullable(createControl(field))
                .ifPresent(control -> {
                    Label label = ofNullable(field.getLabel())
                        .map(translator::translate)
                        .map(control::createAssociatedLabel)
                        .orElse(null);
                    uiStructure.addControl(field.getId(), control, label);
                }));
        uiDescriptor.getButtons()
            .forEach(button -> uiStructure.addButton(button.getId(), createButton(button)));
        ofNullable(uiDescriptor.getDefaultButton())
            .ifPresent(uiStructure::setDefaultButton);

        uiStructure.pack();
        return uiStructure;
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
