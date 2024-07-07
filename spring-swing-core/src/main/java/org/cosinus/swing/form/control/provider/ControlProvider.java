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

package org.cosinus.swing.form.control.provider;

import org.cosinus.swing.form.control.Control;

/**
 * Interface for providing the {@link Control} corresponding to a descriptor
 *
 * @param <R> the type of the real value handled by the control
 */
public interface ControlProvider<R> {

    /**
     * Create a control.
     *
     * @return the created control
     */
    Control<R> getControl();

    /**
     * Create a control based on a descriptor.
     *
     * @param descriptor the control descriptor
     * @return the created control
     * @param <T> the type of the value to show in the control
     */
    <T> Control<R> getControl(final ControlDescriptor<T, R> descriptor);

}
