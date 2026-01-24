/*
 * Copyright 2025 Cosinus Software
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
package org.cosinus.swing.window;

import org.cosinus.swing.dialog.DialogHandler;
import org.cosinus.swing.form.control.Button;
import org.cosinus.swing.ui.UIController;
import org.cosinus.swing.ui.UIModel;
import org.cosinus.swing.ui.UIStructure;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.*;
import java.awt.Frame;

import static java.util.Optional.ofNullable;
import static org.cosinus.swing.ui.UIStructure.CANCEL_BUTTON_ID;
import static org.cosinus.swing.ui.UIStructure.OK_BUTTON_ID;

public class SimpleDialog<M extends UIModel> extends Dialog<M> {

    @Autowired
    protected UIController uiController;

    @Autowired
    protected DialogHandler dialogHandler;

    protected final String descriptorName;

    protected final M model;

    protected UIStructure uiStructure;

    public SimpleDialog(final Frame frame, String title, boolean modal, boolean manageWindowSettings,
                        String descriptorName, M model) {
        super(frame, title, modal, manageWindowSettings);
        this.descriptorName = descriptorName;
        this.model = model;
    }

    public SimpleDialog(final Dialog dialog, String title, boolean modal, boolean manageWindowSettings,
                        String descriptorName, M model) {
        super(dialog, title, modal, manageWindowSettings);
        this.descriptorName = descriptorName;
        this.model = model;
    }

    @Override
    public void initComponents() {
        super.initComponents();

        uiStructure = uiController.createUiStructure(descriptorName);
        getContentPane().add(uiStructure);
        registerDefaultActions(uiStructure);

        uiController.fillUIStructure(uiStructure, model);

        pack();
        centerWindow();

        setDefaultFocus();
    }

    @Override
    protected M getDialogResponse() {
        uiController.updateUIModel(uiStructure, model);
        return model;
    }

    protected void registerDefaultActions(UIStructure uiStructure) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        uiStructure.findButton(OK_BUTTON_ID)
            .ifPresent(this::registerOkAction);
        uiStructure.getActionComponents()
            .forEach(this::registerDoubleClickOkAction);
        uiStructure.findButton(CANCEL_BUTTON_ID)
            .ifPresent(this::registerCancelAction);
        uiStructure.findDefaultButton()
            .ifPresent(uiStructure.getRootPane()::setDefaultButton);
    }

    protected void setDefaultFocus() {
        uiStructure.getFocusComponent()
            .ifPresent(Component::requestFocus);
    }

    @Override
    public void refresh() {
        uiController.fillUIStructure(uiStructure, model);
        super.refresh();
    }
}
