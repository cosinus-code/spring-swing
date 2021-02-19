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

package org.cosinus.swing.boot.model;

import org.cosinus.swing.boot.app.TestSpringSwingComponent;
import org.cosinus.swing.context.SwingApplicationContext;
import org.cosinus.swing.context.SwingAutowired;
import org.cosinus.swing.error.ErrorHandler;
import org.cosinus.swing.form.Dialog;
import org.cosinus.swing.form.Frame;

public class TestDialog extends Dialog<String> {

    public static final String TEST_DIALOG_TITLE = "Test Dialog";

    @SwingAutowired
    public ErrorHandler errorHandler;

    @SwingAutowired
    public TestSpringSwingComponent springSwingComponent;

    public TestDialog(SwingApplicationContext swingContext, Frame frame) {
        super(swingContext, frame, TEST_DIALOG_TITLE, false);
    }
}
