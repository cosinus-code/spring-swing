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
import javax.swing.text.Document;

import java.awt.event.ActionListener;
import java.util.function.Supplier;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Extension of the {@link JTextField}
 * which will automatically inject the application context.
 */
public class TextField extends JTextField implements Control<String> {

    public TextField() {
        injectContext(this);
    }

    public TextField(String text) {
        super(text);
        injectContext(this);
    }

    public TextField(int columns) {
        super(columns);
        injectContext(this);
    }

    public TextField(String text, int columns) {
        super(text, columns);
        injectContext(this);
    }

    public TextField(Document doc, String text, int columns) {
        super(doc, text, columns);
        injectContext(this);
    }

    public TextField(Supplier<ActionListener> actionProvider) {
        super();
        injectContext(this);
        addActionListener(actionProvider.get());
    }

    @Override
    public String getControlValue() {
        return getText();
    }

    @Override
    public void setControlValue(String text) {
        setText(text);
    }
}
