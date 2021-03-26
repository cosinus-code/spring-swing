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

import org.jdatepicker.DateModel;
import org.jdatepicker.impl.DateComponentFormatter;
import org.jdatepicker.impl.JDatePanelImpl;
import org.jdatepicker.impl.JDatePickerImpl;
import org.jdatepicker.impl.UtilDateModel;

import java.util.Date;
import java.util.Properties;

public class DatePicker extends JDatePickerImpl implements Control<Date> {

    public DatePicker(Date date) {
        super(new JDatePanelImpl(new UtilDateModel(date), new Properties()), new DateComponentFormatter());
    }

    @Override
    public DateModel<Date> getModel() {
        return (DateModel<Date>) super.getModel();
    }

    @Override
    public Date getValue() {
        return getModel().getValue();
    }

    @Override
    public void setValue(Date date) {
        getModel().setValue(date);
    }
}
