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

import org.cosinus.swing.validation.ValidationError;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static java.awt.Color.red;
import static java.awt.event.FocusEvent.FOCUS_LOST;
import static java.util.Collections.emptyList;
import static javax.accessibility.AccessibleRelation.LABELED_BY;

/**
 * Interface for a value control.
 *
 * @param <T> the type of the controlled value
 */
public interface Control<T> {

    String LABEL_COLOR = "labelColor";

    Color ERROR_COLOR = red;

    /**
     * Get the control value.
     *
     * @return the control value
     */
    T getControlValue();

    /**
     * Set the control value.
     *
     * @param value the value to set
     */
    void setControlValue(T value);

    /**
     * Validate the control value.
     *
     * @return the list of validation errors
     */
    default List<ValidationError> validateValue() {
        return emptyList();
    }

    /**
     * check if the validation errors should be ignored.
     *
     * @return true if the validation errors should be ignored
     */
    default boolean isIgnoreValidationErrors() {
        return false;
    }

    /**
     * Create the associated control label.
     *
     * @param label the label text
     * @return the created label
     */
    default Label createAssociatedLabel(String label) {
        Label controlLabel = new Label(label);
        Optional.of(this)
            .filter(control -> JComponent.class.isAssignableFrom(control.getClass()))
            .map(JComponent.class::cast)
            .ifPresent(controlLabel::setLabelFor);
        return controlLabel;
    }

    /**
     * Get the associated control label, if any.
     *
     * @return the associated control label, or {@link Optional#empty()}
     */
    default Optional<JLabel> getAssociatedLabel() {
        return getControlProperty(LABELED_BY, JLabel.class);
    }

    /**
     * Get the associated control label color, if any.
     *
     * @return the associated control label color, or {@link Optional#empty()}
     */
    default Optional<Color> getAssociatedLabelColor() {
        return getControlProperty(LABEL_COLOR, Color.class);
    }

    /**
     * Set the associated control label color.
     *
     * @param color the color to set
     */
    default void setAssociatedLabelColor(Color color) {
        setControlProperty(LABEL_COLOR, color);
    }

    /**
     * Get the value for a control property.
     *
     * @param propertyName  the property name to look for
     * @param propertyClass the expected property value class
     * @param <P>           the type of the property value
     * @return the value of the property, or {@link Optional#empty()}
     */
    default <P> Optional<P> getControlProperty(String propertyName, Class<P> propertyClass) {
        return Optional.of(this)
            .filter(control -> JComponent.class.isAssignableFrom(control.getClass()))
            .map(JComponent.class::cast)
            .map(component -> component.getClientProperty(propertyName))
            .filter(property -> propertyClass.isAssignableFrom(property.getClass()))
            .map(propertyClass::cast);
    }

    /**
     * Set the value for a control property.
     *
     * @param propertyName  the property name to set the value for
     * @param propertyValue the value to set
     * @param <P>           the type of the property value
     */
    default <P> void setControlProperty(String propertyName, P propertyValue) {
        Optional.of(this)
            .filter(control -> JComponent.class.isAssignableFrom(control.getClass()))
            .map(JComponent.class::cast)
            .ifPresent(component -> component.putClientProperty(propertyName, propertyValue));
    }

    /**
     * Set the control to normal state.
     */
    default void setNormalState() {
        getAssociatedLabelColor()
            .ifPresent(color -> getAssociatedLabel()
                .ifPresent(label -> label.setForeground(color)));
        setAssociatedLabelColor(null);
    }

    /**
     * Set the control error state.
     */
    default void setErrorState() {
        getAssociatedLabel().ifPresent(label -> {
            if (getAssociatedLabelColor().isEmpty()) {
                setAssociatedLabelColor(label.getForeground());
            }
            label.setForeground(getErrorColor());
        });
    }

    /**
     * Get the error color associated with this control (default red).
     *
     * @return the error color
     */
    default Color getErrorColor() {
        return ERROR_COLOR;
    }

    /**
     * Process the focus event on this control.
     *
     * @param event               the fired focus event
     * @param focusEventProcessor the focus event processor
     */
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

    /**
     * Create validation error with a a given code.
     *
     * @param code then error code
     * @return the created validation error
     */
    default ValidationError createValidationError(String code) {
        return getAssociatedLabel()
            .map(JLabel::getText)
            .map(label -> new ValidationError(code, label))
            .orElseGet(() -> new ValidationError(code));
    }

    /**
     * Update the associated label text.
     *
     * @param labelText the new label text
     */
    default void updateAssociatedLabel(String labelText) {
        getAssociatedLabel().ifPresent(label -> label.setText(labelText));
    }

    /**
     * Return this as a {@link Component}.
     *
     * @return this as a {@link Component}
     */
    default Component getComponent() {
        return (Component) this;
    }

    /**
     * Enable/disable this control.
     *
     * @param enabled true to enable the control, false otherwise
     */
    default void setControlEnabled(boolean enabled) {
        getComponent().setEnabled(enabled);
        getAssociatedLabel().ifPresent(label -> label.setEnabled(enabled));
    }

    default void addActionListener(final ActionListener actionListener) {
    }
}
