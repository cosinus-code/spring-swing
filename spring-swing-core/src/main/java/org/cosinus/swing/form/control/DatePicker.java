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

package org.cosinus.swing.form.control;

import org.jdatepicker.DateModel;
import org.jdatepicker.JDatePicker;
import org.jdatepicker.UtilDateModel;

import java.awt.event.ActionListener;
import java.util.Date;

/**
 * Extension of the {@link JDatePicker}
 * which will automatically inject the application context.
 */
public class DatePicker extends JDatePicker implements Control<Date> {

    public DatePicker(Date date) {
        super(new UtilDateModel(date));
    }

    @Override
    public DateModel<Date> getModel() {
        return (DateModel<Date>) super.getModel();
    }

    @Override
    public Date getControlValue() {
        return getModel().getValue();
    }

    @Override
    public void setControlValue(Date date) {
        getModel().setValue(date);
    }

    @Override
    public void addActionListener(ActionListener actionListener) {
        super.addActionListener(actionListener);
    }
}
