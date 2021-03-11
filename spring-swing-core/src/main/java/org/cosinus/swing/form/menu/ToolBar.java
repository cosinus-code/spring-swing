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

package org.cosinus.swing.form.menu;

import org.cosinus.swing.context.SwingApplicationContext;
import org.cosinus.swing.context.SwingInject;
import org.cosinus.swing.form.FormComponent;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

/**
 * Toolbar menu model
 */
public class ToolBar extends JToolBar implements SwingInject, FormComponent {

    public ToolBar() {
        injectSwingContext(SwingApplicationContext.instance);
    }

    @Override
    public void initComponents() {
        setOrientation(JToolBar.VERTICAL);
        setFloatable(false);
        setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
    }

    @Override
    public void initContent() {
    }

    @Override
    public void translate() {
    }
}
