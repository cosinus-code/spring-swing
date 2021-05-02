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
import java.util.Optional;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Extension of the {@link JCheckBox}
 * which will automatically inject the application context.
 */
public class CheckBox extends JCheckBox implements Control<Boolean> {

    public CheckBox() {
        injectContext(this);
    }

    public CheckBox(Icon icon) {
        super(icon);
        injectContext(this);
    }

    public CheckBox(Icon icon, boolean selected) {
        super(icon, selected);
        injectContext(this);
    }

    public CheckBox(String text) {
        super(text);
        injectContext(this);
    }

    public CheckBox(Action a) {
        super(a);
        injectContext(this);
    }

    public CheckBox(String text, boolean selected) {
        super(text, selected);
        injectContext(this);
    }

    public CheckBox(String text, Icon icon) {
        super(text, icon);
        injectContext(this);
    }

    public CheckBox(String text, Icon icon, boolean selected) {
        super(text, icon, selected);
        injectContext(this);
    }

    @Override
    public Boolean getControlValue() {
        return isSelected();
    }

    @Override
    public void setControlValue(Boolean selected) {
        setSelected(selected);
    }

    @Override
    public JLabel createAssociatedLabel(String labelText) {
        updateAssociatedLabel(labelText);
        return new JLabel();
    }

    @Override
    public Optional<JLabel> getAssociatedLabel() {
        return Optional.empty();
    }

    @Override
    public void updateAssociatedLabel(String labelText) {
        setText(labelText);
    }
}
