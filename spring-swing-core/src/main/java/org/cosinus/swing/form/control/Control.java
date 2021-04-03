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

import org.cosinus.swing.validation.ValidationError;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.awt.Color.red;
import static java.awt.event.FocusEvent.FOCUS_LOST;
import static java.util.Collections.emptyList;
import static javax.accessibility.AccessibleRelation.LABELED_BY;

public interface Control<T> {

    String LABEL_COLOR = "labelColor";

    Color ERROR_COLOR = red;

    T getControlValue();

    void setControlValue(T value);

    default List<ValidationError> validateValue() {
        return emptyList();
    }

    default boolean isIgnoreValidationErrors() {
        return false;
    }

    default JLabel createAssociatedLabel(String label) {
        JLabel controlLabel = new JLabel(label);
        Optional.of(this)
            .filter(control -> JComponent.class.isAssignableFrom(control.getClass()))
            .map(JComponent.class::cast)
            .ifPresent(controlLabel::setLabelFor);
        return controlLabel;
    }

    default Optional<JLabel> getAssociatedLabel() {
        return getClientProperty(LABELED_BY, JLabel.class);
    }

    default Optional<Color> getAssociatedLabelColor() {
        return getClientProperty(LABEL_COLOR, Color.class);
    }

    default void setAssociatedLabelColor(Color color) {
        setClientProperty(LABEL_COLOR, color);
    }

    default <T> Optional<T> getClientProperty(String propertyName, Class<T> propertyClass) {
        return Optional.of(this)
            .filter(control -> JComponent.class.isAssignableFrom(control.getClass()))
            .map(JComponent.class::cast)
            .map(component -> component.getClientProperty(propertyName))
            .filter(property -> propertyClass.isAssignableFrom(property.getClass()))
            .map(propertyClass::cast);
    }

    default <T> void setClientProperty(String propertyName, T propertyValue) {
        Optional.of(this)
            .filter(control -> JComponent.class.isAssignableFrom(control.getClass()))
            .map(JComponent.class::cast)
            .ifPresent(component -> component.putClientProperty(propertyName, propertyValue));
    }

    default void setNormalState() {
        getAssociatedLabelColor()
            .ifPresent(color -> getAssociatedLabel()
                .ifPresent(label -> label.setForeground(color)));
        setAssociatedLabelColor(null);
    }

    default void setErrorState() {
        getAssociatedLabel().ifPresent(label -> {
            if (getAssociatedLabelColor().isEmpty()) {
                setAssociatedLabelColor(label.getForeground());
            }
            label.setForeground(getErrorColor());
        });
    }

    default Color getErrorColor() {
        return ERROR_COLOR;
    }

    default void processFocusEvent(FocusEvent event, Consumer<FocusEvent> focusEventProcessor) {
        focusEventProcessor.accept(event);
        if (!isIgnoreValidationErrors()) {
            if (event.getID() == FOCUS_LOST && !validateValue().isEmpty()) {
                setErrorState();
            } else {
                setNormalState();
            }
        }
    }

    default ValidationError createValidationError(String code) {
        return getAssociatedLabel()
            .map(JLabel::getText)
            .map(label -> new ValidationError(code, label))
            .orElseGet(() -> new ValidationError(code));
    }
}
