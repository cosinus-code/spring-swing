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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cosinus.swing.error.ValidationException;
import org.cosinus.swing.validation.ValidationError;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.text.Format;
import java.text.ParseException;
import java.util.List;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Optional.ofNullable;
import static org.cosinus.swing.context.ApplicationContextInjector.injectContext;
import static org.springframework.util.NumberUtils.convertNumberToTargetClass;

/**
 * Extension of the {@link JFormattedTextField}
 * which will automatically inject the application context.
 * <p>
 * It supports the full {@link Control} functionality of highlighting the validation errors.
 *
 * @param <T> the type of the value
 */
public class NumberFormattedTextField<T extends Number> extends JFormattedTextField implements Control<T> {

    private static final Logger LOG = LogManager.getLogger(NumberFormattedTextField.class);

    private final Class<T> valueClass;

    private T value;

    public NumberFormattedTextField(Format format, Class<T> valueClass) {
        super(format);
        injectContext(this);
        this.valueClass = valueClass;
    }

    public NumberFormattedTextField(AbstractFormatter formatter, Class<T> valueClass) {
        super(formatter);
        injectContext(this);
        this.valueClass = valueClass;
    }

    public NumberFormattedTextField(AbstractFormatterFactory factory, Class<T> valueClass) {
        super(factory);
        injectContext(this);
        this.valueClass = valueClass;
    }

    public NumberFormattedTextField(AbstractFormatterFactory factory, Object currentValue, Class<T> valueClass) {
        super(factory, currentValue);
        injectContext(this);
        this.valueClass = valueClass;
    }

    @Override
    public T getControlValue() {
        return value;
    }

    @Override
    public void setControlValue(T value) {
        super.setValue(value);
    }

    @Override
    public void commitEdit() {
        if (getText().isEmpty()) {
            this.value = null;
            return;
        }
        try {
            super.commitEdit();
            this.value = ofNullable(super.getValue())
                .map(this::convertValue)
                .orElse(null);
        } catch (ParseException | IllegalArgumentException ex) {
            this.value = null;
            throw new ValidationException(format("Cannot convert '%s' to %s", getText(), valueClass), ex);
        }

    }

    protected T convertValue(Object wildValue) {
        return convertNumberToTargetClass((Number) wildValue, valueClass);
    }

    @Override
    public List<ValidationError> validateValue() {
        try {
            commitEdit();
            return emptyList();
        } catch (ValidationException e) {
            LOG.error("Invalid formatted value", e);
            return singletonList(createValidationError("validation.invalidNumber"));
        }
    }

    @Override
    public void processFocusEvent(FocusEvent e) {
        try {
            super.processFocusEvent(e);
            setNormalState();
        } catch (ValidationException ex) {
            setErrorState();
            throw ex;
        }
    }
}
