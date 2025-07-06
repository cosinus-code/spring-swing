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

import javax.swing.*;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Extension of the {@link JLabel}
 * which will automatically inject the application context.
 */
public class Label extends JLabel implements Control<Object> {

    public Label(String text, Icon icon, int horizontalAlignment) {
        super(text, icon, horizontalAlignment);
        injectContext(this);
    }

    public Label(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
        injectContext(this);
    }

    public Label(String text) {
        super(text);
        injectContext(this);
    }

    public Label(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
        injectContext(this);
    }

    public Label(Icon image) {
        super(image);
        injectContext(this);
    }

    public Label() {
        injectContext(this);
    }

    @Override
    public String getControlValue() {
        return getText();
    }

    @Override
    public void setControlValue(Object value) {
        ofNullable(value)
            .map(Object::toString)
            .ifPresent(this::setText);

        if (value instanceof ControlValue controlValue) {
            ofNullable(controlValue.getIcon())
                .ifPresent(this::setIcon);
            ofNullable(controlValue.getTooltip())
                .ifPresent(this::setToolTipText);
        }
    }
}
