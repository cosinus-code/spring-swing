/*
 * Copyright 2026 Cosinus Software
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

import java.awt.event.ActionListener;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Extension of the {@link JPasswordField}
 * which will automatically inject the application context.
 */
public class PasswordField extends JPasswordField implements Control<String> {

    public PasswordField() {
        injectContext(this);
    }

    public PasswordField(String text) {
        super(text);
        injectContext(this);
    }

    public PasswordField(int columns) {
        super(columns);
        injectContext(this);
    }

    @Override
    public String getControlValue() {
        return new String(getPassword());
    }

    @Override
    public void setControlValue(String text) {
        setText(text);
    }

    @Override
    public void addActionListener(ActionListener actionListener) {
        super.addActionListener(actionListener);
    }
}
