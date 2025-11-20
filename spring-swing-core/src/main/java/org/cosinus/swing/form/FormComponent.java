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

package org.cosinus.swing.form;

import java.awt.*;
import java.util.stream.Stream;

import static java.util.Arrays.stream;
import static org.cosinus.stream.Streams.flatComponentsStream;

/**
 * Generic form component interface
 */
public interface FormComponent {

    /**
     * Initialize form components.
     */
    default void initComponents() {
    }

    /**
     * Update this form component.
     * It is called on initialization and whenever the ui theme is changed.
     */
    default void updateForm() {
    }

    /**
     * Trigger the for update starting with form component down on the component tree
     */
    default void triggerFormUpdate() {
        flatStreamFormComponents()
            .forEach(FormComponent::updateForm);
    }

    default Stream<FormComponent> streamFormComponents() {
        return this instanceof Container container ?
            stream(container.getComponents())
                .filter(FormComponent.class::isInstance)
                .map(FormComponent.class::cast) :
            Stream.empty();
    }

    default Stream<FormComponent> flatStreamFormComponents() {
        return this instanceof Container container ?
            flatComponentsStream(container)
                .filter(component -> FormComponent.class.isAssignableFrom(component.getClass()))
                .map(FormComponent.class::cast) :
            Stream.empty();
    }

    /**
     * Translate form.
     */
    default void translate() {
    }
}
