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
package org.cosinus.swing.ui;

import java.util.ArrayList;
import java.util.List;

public class UIDescriptor {

    private String title;

    private UILayout layout;

    private List<UIField> fields = new ArrayList<>();

    private List<UIButton> buttons = new ArrayList<>();

    private String defaultButton;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public UILayout getLayout() {
        return layout;
    }

    public void setLayout(UILayout layout) {
        this.layout = layout;
    }

    public List<UIField> getFields() {
        return fields;
    }

    public void setFields(List<UIField> fields) {
        this.fields = fields;
    }

    public List<UIButton> getButtons() {
        return buttons;
    }

    public void setButtons(List<UIButton> buttons) {
        this.buttons = buttons;
    }

    public String getDefaultButton() {
        return defaultButton;
    }

    public void setDefaultButton(String defaultButton) {
        this.defaultButton = defaultButton;
    }
}
