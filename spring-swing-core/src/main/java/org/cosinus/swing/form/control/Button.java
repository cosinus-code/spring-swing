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

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Extension of the {@link JButton}
 * which will automatically inject the application context.
 */
public class Button extends JButton implements Control<String> {

    public Button() {
        injectContext(this);
    }

    public Button(Icon icon) {
        super(icon);
        injectContext(this);
    }

    public Button(String text) {
        super(text);
        injectContext(this);
    }

    public Button(Action action) {
        super(action);
        injectContext(this);
    }

    public Button(String text, Icon icon) {
        super(text, icon);
        injectContext(this);
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
