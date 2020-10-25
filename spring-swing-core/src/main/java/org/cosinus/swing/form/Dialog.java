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

import org.cosinus.swing.context.SwingApplicationContext;
import org.cosinus.swing.context.SwingInject;

import javax.swing.*;
import java.util.Optional;

/**
 * Abstract dialog window with basic functionality
 */
public abstract class Dialog<T> extends JDialog implements Window, SwingInject, FormComponent {

    private boolean cancelled;

    public Dialog(SwingApplicationContext context,
                  Frame frame, String title, boolean modal) {
        super(frame, title, modal);
        injectSwingContext(context);
        init();
    }

    public Dialog(SwingApplicationContext context,
                  Dialog dialog, String title, boolean modal) {
        super(dialog, title, modal);
        injectSwingContext(context);
        init();
    }

    private void init() {
        centerWindow();
        registerExitOnEscapeKey();
    }

    public Optional<T> response() {
        return isCancelled() ?
                Optional.empty() :
                Optional.ofNullable(getDialogResponse());
    }

    protected T getDialogResponse() {
        return null;
    }

    public void cancel() {
        cancelled = true;
        dispose();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void initComponents() {

    }

    @Override
    public void updateFormUI() {

    }

    @Override
    public void translateForm() {

    }

    @Override
    public void initContent() {

    }
}