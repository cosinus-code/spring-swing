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

package org.cosinus.swing.form;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.function.Supplier;

import static javax.swing.KeyStroke.getKeyStroke;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Extension of the {@link JPanel}
 * which will automatically inject the application context.
 */
public class Panel extends JPanel implements FormComponent {

    public Panel() {
        injectContext(this);
    }

    public Panel(LayoutManager layout) {
        super(layout);
        injectContext(this);
    }

    @Override
    public void initComponents() {
    }

    @Override
    public void translate() {
    }

    public void registerEscapeAction(Supplier<ActionListener> actionProvider) {
        registerKeyboardAction(actionProvider.get(),
            getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            WHEN_IN_FOCUSED_WINDOW);
    }
}
