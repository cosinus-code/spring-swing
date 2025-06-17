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

package org.cosinus.swing.form.control.provider;

import org.cosinus.swing.form.control.Control;
import org.cosinus.swing.form.control.Label;
import org.cosinus.swing.form.control.LookAndFeelControl;

import static org.apache.commons.collections4.CollectionUtils.isEmpty;

/**
 * Implementation of {@link ControlProvider}
 * for providing the {@link Control} corresponding to a look-and-feel value.
 */
public class LookAndFeelControlProvider implements ControlProvider<String> {

    @Override
    public Control<String> getControl() {
        //TODO
        return new Label();
    }

    @Override
    public <T> Control<String> getControl(ControlDescriptor<T, String> descriptor) {
        return isEmpty(descriptor.getValues()) ?
            new Label(descriptor.getRealValue()) :
            new LookAndFeelControl(descriptor.getRealValues(), descriptor.getRealValue());
    }
}
