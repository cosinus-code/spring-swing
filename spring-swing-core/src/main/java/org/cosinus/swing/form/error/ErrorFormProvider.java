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

package org.cosinus.swing.form.error;

import org.cosinus.swing.context.SpringSwingComponent;
import org.cosinus.swing.context.SwingApplicationContext;
import org.cosinus.swing.form.Dialog;
import org.cosinus.swing.form.Frame;

import java.awt.*;
import java.util.Optional;

/**
 * Error form provider
 */
@SpringSwingComponent
public class ErrorFormProvider {

    private final SwingApplicationContext swingContext;

    public ErrorFormProvider(SwingApplicationContext swingContext) {
        this.swingContext = swingContext;
    }


    public Optional<ErrorForm> getErrorForm(Window parent) {
        return Optional.of(parent instanceof Dialog<?> ?
                               new DefaultErrorForm(swingContext, (Dialog<?>) parent, true) :
                               new DefaultErrorForm(swingContext, (Frame) parent, true));
    }
}
