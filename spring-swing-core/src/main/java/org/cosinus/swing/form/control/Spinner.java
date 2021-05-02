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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;

/**
 * Extension of the {@link JSpinner}
 * which will automatically inject the application context.
 *
 * @param <T> the type of the value
 */
public class Spinner<T> extends JSpinner implements Control<T> {

    private static final Logger LOG = LogManager.getLogger(Spinner.class);

    public Spinner() {
        injectContext(this);
    }

    public Spinner(T value) {
        injectContext(this);
        setControlValue(value);
    }

    public Spinner(SpinnerModel model) {
        super(model);
        injectContext(this);
    }

    @Override
    public T getControlValue() {
        return (T) super.getValue();
    }

    @Override
    public void setControlValue(T value) {
        super.setValue(value);
    }
}
